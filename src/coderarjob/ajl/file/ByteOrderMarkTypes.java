/**
 * This part of the Arjobs Java Library.
 * ByteOrderMark Types.
 **/
package coderarjob.ajl.file;

public enum ByteOrderMarkTypes
{
  UTF8        (0xEF, 0xBB, 0xBF), 
  UTF16_BE    (0xFE, 0xFF),
  UTF16_LE    (0xFF, 0xFE),
  NOT_PRESENT ();

  private final byte[] mBomBytes;

  private ByteOrderMarkTypes () 
  {
    this.mBomBytes = new byte[] {};
  }

  private ByteOrderMarkTypes (int msb, int b0) 
  {
    this.mBomBytes = new byte[] {(byte)msb, (byte)b0};
  }

  private ByteOrderMarkTypes (int msb, int b1, int b0) 
  {
    this.mBomBytes = new byte[] {(byte)msb, (byte)b1, (byte)b0};
  }

  public byte[] getBomBytes() { return this.mBomBytes; }
}
