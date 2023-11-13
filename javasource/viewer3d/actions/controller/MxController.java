package viewer3d.actions.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.regex.Pattern;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.*;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.viewer3d.jtreader.JtReader;
import com.mendix.viewer3d.jtreader.StreamType;

import viewer3d.actions.JtResponseWriter;
import viewer3d.actions.MendixFileDocumentDataProvider;
import viewer3d.actions.ModelType;
import viewer3d.actions.httprouter.annotation.*;

import viewer3d.proxies.MxModelDocument;
import viewer3d.proxies.Markup;
import viewer3d.proxies.MxChildDocument;

public class MxController {

    private ILogNode logger;

    static String VERSION_PATTERN = "(\\d+)[.](\\d+)";
    
    static String INVALID_VERSION = "Only JT models with version starting from 9.0 are supported";
    
    static String INVALID_ENTRY_PATH = "Invalid entry path";
    
    static String INVALID_ZIP_FILE = "The zipped Model file is invalid";

    public MxController() {
        this.logger = Core.getLogger(MxController.class.getSimpleName());
    }

    @PostMapping("mx/models")
    public void postModel(
    		@Context IContext ctx,
    		@Request IMxRuntimeRequest request,
            @Response IMxRuntimeResponse response, 
            @FormPart("file") InputStream file, 
            @FormPart("modelType") String modelTypeStr,
            @FormPart("name") String name, 
            @FormPart("entryPath") String entryPath) throws Exception {

        var modelType = ModelType.values()[Integer.parseInt(modelTypeStr)];
        MxModelDocument obj = null;
        if (name.toLowerCase().endsWith(".zip")) {
            obj = this.saveZippedFile(ctx, response, file, entryPath, modelType);
        } else {
            obj = this.saveFile(ctx, file, name, modelType);
        }

        if (obj != null) {
            var modelId = obj.getModelId();
            var oStream = response.getOutputStream();
            oStream.write(modelId.getBytes());
            response.setContentType("text/plain;charset=utf-8");
            response.setStatus(IMxRuntimeResponse.OK);
            oStream.close();
        }
    }

    @PostMapping("mx/{modelId}/markups")
    public void postMarkup(@Context IContext ctx, @Request IMxRuntimeRequest request,
            @Response IMxRuntimeResponse response, @PathVariable("modelId") String modelId,
            @FormPart("title") String title, @FormPart("width") String width, @FormPart("height") String height,
            @FormPart("snapshot") InputStream snapshot) throws Exception {
        this.logger.info("Upload Markup start");

        if (!Util.isValidModelId(modelId)) {
            this.logger.error("Invalid modelId");
            response.setStatus(IMxRuntimeResponse.BAD_REQUEST);
            return;
        }

        // get the mxModelDocument by modelId
        var mendixObjects = Core.retrieveXPathQuery(ctx, "//Viewer3D.MxModelDocument[ModelId='" + modelId + "']");
        if (mendixObjects.isEmpty()) {
            this.logger.error("No Associated MxModelDocument founded for" + modelId);
            response.setStatus(IMxRuntimeResponse.NOT_FOUND);
            return;
        }

        // create mx object
        var docObj = new Markup(ctx);

        // store file content
        var req = request.getHttpServletRequest();

        // always use the first mendix object
        var mxDocument = MxModelDocument.initialize(ctx, mendixObjects.get(0));
        docObj.setMarkup_MxModelDocument(ctx, mxDocument);

        var mxName = title;
        if (mxName.isEmpty()) {
	        var mendixObject = mxDocument.getMendixObject();

	        String userModelId;
	        if (modelId.startsWith("usermodel&")) {
	        	userModelId = modelId.substring(10);
		        var fds = Core.retrieveXPathQuery(ctx, "//System.FileDocument[FileID='" + userModelId + "']");
		        if (fds.size() > 0) {
		        	mendixObject = fds.get(0);
		        }     
	        }

	        mxName = (String) mendixObject.getMember(ctx, "Name").getValue(ctx);
            if (!mxName.isEmpty()) {
                var commaIndex = mxName.lastIndexOf(".");
                if (commaIndex != -1)
                    mxName = mxName.substring(0, commaIndex);
            }
        }

        mxName += ".png";

        docObj.setName(ctx, mxName);
        this.logger.info("MxName: " + mxName);

        var thumbnailWidth = Float.parseFloat(width) * 0.25;
        var thumbnailHeight = Float.parseFloat(height) * 0.25;
        this.logger.info("Width: " + thumbnailWidth + "," + "height:" + thumbnailHeight);
        Core.storeImageDocumentContent(ctx, docObj.getMendixObject(), snapshot, (int) thumbnailWidth, (int) thumbnailHeight);
        Core.commit(ctx, docObj.getMendixObject());

        response.setContentType("text/plain;charset=utf-8");
        response.setStatus(IMxRuntimeResponse.OK);
    }

    @GetMapping("mx/{modelId}/ps")
    public void getPs(@Context IContext ctx, @Request IMxRuntimeRequest request, @Response IMxRuntimeResponse response,
            @PathVariable("modelId") String modelId) throws Exception {
        this.processJtRequest(ctx, request, response, modelId, StreamType.ProductStructure);
    }

    @PostMapping("mx/{modelId}/shapes")
    public void getShapes(@Context IContext ctx, @Request IMxRuntimeRequest request,
            @Response IMxRuntimeResponse response, @PathVariable("modelId") String modelId) throws Exception {
        this.processJtRequest(ctx, request, response, modelId, StreamType.Shapes);
    }

    @PostMapping("mx/{modelId}/pmi")
    public void getPmi(@Context IContext ctx, @Request IMxRuntimeRequest request, @Response IMxRuntimeResponse response,
            @PathVariable("modelId") String modelId) throws Exception {
        this.processJtRequest(ctx, request, response, modelId, StreamType.Pmi);
    }

    @PostMapping("mx/{modelId}/metadata")
    public void getMetadata(@Context IContext ctx, @Request IMxRuntimeRequest request,
            @Response IMxRuntimeResponse response, @PathVariable("modelId") String modelId) throws Exception {
        this.processJtRequest(ctx, request, response, modelId, StreamType.Metadata);
    }

    private void processJtRequest(IContext ctx, IMxRuntimeRequest request, IMxRuntimeResponse response, String modelId,
            StreamType st) throws Exception {
        if (!Util.isValidModelId(modelId)) {
            response.setStatus(IMxRuntimeResponse.BAD_REQUEST);
            return;
        }

    	boolean userModel = modelId.startsWith("usermodel&");
    	IMendixObject mendixObject;
        if (!userModel) {
	        var mendixObjects = Core.retrieveXPathQuery(ctx, "//Viewer3D.MxModelDocument[ModelId='" + modelId + "']");
	        if (mendixObjects.size() == 0) {
	            mendixObjects = Core.retrieveXPathQuery(ctx, "//Viewer3D.MxChildDocument[ModelId='" + modelId + "']");
	            if (mendixObjects.size() == 0) {
	                response.setStatus(IMxRuntimeResponse.NOT_FOUND);
	                return;
	            }
	        }
	        mendixObject = mendixObjects.get(0);
	    } else {
	        String userModelId = modelId.substring(10);
		    var fds = Core.retrieveXPathQuery(ctx, "//System.FileDocument[FileID='" + userModelId + "']");
		    if (fds.size() > 0) {
		    	mendixObject = fds.get(0);
		    } else {
                response.setStatus(IMxRuntimeResponse.NOT_FOUND);
                return;
		    }
	    }

        var ids = Util.getSegmentIds(request, st);
        var size = (long) mendixObject.getMember(ctx, "Size").getValue(ctx);
        var dp = new MendixFileDocumentDataProvider(ctx, mendixObject, size);
        var jtReader = new JtReader(dp);
        var writer = new JtResponseWriter(response, modelId);
        jtReader.pipe(st, writer, ids);
        response.setContentType("application/octet-stream");
    }

    private MxModelDocument saveZippedFile(IContext ctx, IMxRuntimeResponse response, InputStream stream,
            String entryFileName, ModelType modelType) throws IOException, CoreException {
        if (!entryFileName.toLowerCase().endsWith(".jt")) {
            entryFileName += ".jt";
        }

        ZipInputStream zipStream = new ZipInputStream(stream);
        ZipEntry entry;
        MxModelDocument entryObj = null;
        var entries = new java.util.ArrayList<MxChildDocument>();
        String error = null;
        while (error == null && (entry = zipStream.getNextEntry()) != null) {
            String fileName = entry.getName();
            if (!entry.isDirectory() && fileName.toLowerCase().endsWith(".jt")) {
                var size = entry.getSize();
                var buffer = new byte[(int) size];
                var pos = 0;
                var count = -1;

                while (true) {
                    count = zipStream.read(buffer, pos, buffer.length - pos);
                    if (count <= 0) {
                        break;
                    }
                    pos += count;
                }

                if (this.checkJTVersion(buffer)) {
                    var output = new ByteArrayInputStream(buffer);
                    if (entryFileName.equalsIgnoreCase(fileName)) {
                        entryObj = new MxModelDocument(ctx);
                        var modelId = UUID.randomUUID().toString();
                        entryObj.setModelId(modelId);
                        var modelTypeStr = modelType.toString();
                        entryObj.setModelType(modelTypeStr);

                        Core.storeFileDocumentContent(ctx, entryObj.getMendixObject(), fileName, output);
                    } else {
                        var modelEntry = new MxChildDocument(ctx);
                        Core.storeFileDocumentContent(ctx, modelEntry.getMendixObject(), fileName, output);
                        modelEntry.setModelId(UUID.randomUUID().toString());
                        entries.add(modelEntry);

                    }
                } else {
                	error = INVALID_VERSION;
                }
            }
            
            zipStream.closeEntry();
        }

        zipStream.close();
        
        if(error == null) {
        	if(entryObj == null) {
        		error = INVALID_ENTRY_PATH;
        	} else if(entries.size() == 0) {
        		error = INVALID_ZIP_FILE;
        	}
        }
        
        if(error != null) {
            var writer = response.getWriter();
          	writer.append(error);
          	response.setStatus(IMxRuntimeResponse.DATAVALIDATION_ERROR_CODE);
          	writer.flush();
        } else {
        	for (var item : entries) {
                Core.commit(ctx, item.getMendixObject());
            }

            entryObj.setModelEntries(entries);

            // set status to InProgress
            entryObj.setStatus("InProgress");
            Core.commit(ctx, entryObj.getMendixObject());
            entryObj.setStatus("Complete");
            Core.commit(ctx, entryObj.getMendixObject());
        }


        return entryObj;
    }

    private MxModelDocument saveFile(IContext ctx, InputStream stream, String name, ModelType modelType)
            throws CoreException {
        // create mx object
        var docObj = new MxModelDocument(ctx);
        var mxObj = docObj.getMendixObject();
        var modelId = UUID.randomUUID().toString();
        docObj.setModelId(modelId);
        var modelTypeStr = modelType.toString();
        docObj.setModelType(modelTypeStr);

        Core.storeFileDocumentContent(ctx, mxObj, name, stream);

        // set status to InProgress
        docObj.setStatus("InProgress");
        Core.commit(ctx, mxObj);

        // perform other tasks in the future

        // set status to Complete
        docObj.setStatus("Complete");
        Core.commit(ctx, mxObj);
        return docObj;
    }

    private boolean checkJTVersion(byte[] buffer) {
        if (buffer.length < 80) {
            return false;
        }

        var str = new String(buffer, 0, 80);
        var r = Pattern.compile(MxController.VERSION_PATTERN);
        var m = r.matcher(str);
        if (m.find()) {
            if (m.groupCount() > 0) {
                var version = m.group(0).split("[.]");
                try {
                    var major = Integer.parseInt(version[0], 10);
                    if (major < 9) {
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    return false;
                }

                return true;
            }
        }

        return false;
    }
}
