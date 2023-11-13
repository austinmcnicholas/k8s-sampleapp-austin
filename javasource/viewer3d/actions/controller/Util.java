package viewer3d.actions.controller;

import java.io.IOException;
import java.util.*;

import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.viewer3d.jtreader.JtUtils;
import com.mendix.viewer3d.jtreader.StreamType;

public class Util {
    public static boolean isValidModelId(String modelId) {
        if (modelId.startsWith("usermodel&")) {
            String userModelId = modelId.substring(10);
            try {
                Integer.parseInt(userModelId);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        } else {
            try {
                UUID.fromString(modelId);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }

    public static Set<UUID> getSegmentIds(IMxRuntimeRequest request, StreamType st) throws IOException {
        if (st == StreamType.ProductStructure) {
            return null;
        }
        return JtUtils.INSTANCE.parseSegmentIds(request.getInputStream());
    }
}
