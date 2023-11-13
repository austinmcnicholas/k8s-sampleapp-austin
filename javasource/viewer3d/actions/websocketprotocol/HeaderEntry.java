// automatically generated by the FlatBuffers compiler, do not modify

package viewer3d.actions.websocketprotocol;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class HeaderEntry extends Table {
  public static HeaderEntry getRootAsHeaderEntry(ByteBuffer _bb) { return getRootAsHeaderEntry(_bb, new HeaderEntry()); }
  public static HeaderEntry getRootAsHeaderEntry(ByteBuffer _bb, HeaderEntry obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public HeaderEntry __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String value() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer valueAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer valueInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createHeaderEntry(FlatBufferBuilder builder,
      int nameOffset,
      int valueOffset) {
    builder.startObject(2);
    HeaderEntry.addValue(builder, valueOffset);
    HeaderEntry.addName(builder, nameOffset);
    return HeaderEntry.endHeaderEntry(builder);
  }

  public static void startHeaderEntry(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addValue(FlatBufferBuilder builder, int valueOffset) { builder.addOffset(1, valueOffset, 0); }
  public static int endHeaderEntry(FlatBufferBuilder builder) {
    int o = builder.endObject();
    builder.required(o, 4);  // name
    builder.required(o, 6);  // value
    return o;
  }
}

