package clearcl.ops.render.test;

import static org.junit.Assert.*;

import javax.vecmath.Matrix4f;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.ops.render.ImageRender;
import clearcl.ops.render.enums.Algorithm;
import clearcl.ops.render.enums.Parameter;
import clearcl.test.ClearCLBasicTests;
import clearcl.util.Region2;
import clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;

/**
 *
 *
 * @author royer
 */
public class ImageRenderTest
{

  @Test
  public void test() throws InterruptedException
  {
    ClearCLBackendInterface lClearCLBackend =
                                            ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackend))
    {

      ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

      System.out.println(lBestGPUDevice.getInfoString());

      ClearCLContext lContext = lBestGPUDevice.createContext();

      int lWidth = 256;
      int lHeight = 256;
      int lDepth = 256;

      ClearCLImage l3DImage =
                            lContext.createSingleChannelImage(HostAccessType.WriteOnly,
                                                              KernelAccessType.ReadWrite,
                                                              ImageChannelDataType.UnsignedInt8,
                                                              lWidth,
                                                              lHeight,
                                                              lDepth);

      final byte[] lVolumeDataArray = new byte[lWidth * lHeight
                                               * lDepth];

      for (int z = 0; z < lDepth; z++)
        for (int y = 0; y < lHeight; y++)
          for (int x = 0; x < lWidth; x++)
          {
            final int lIndex = x + lWidth * y + lWidth * lHeight * z;
            int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
            if (lCharValue < 12)
              lCharValue = 0;
            lVolumeDataArray[lIndex] = (byte) lCharValue;
          }

      l3DImage.readFrom(lVolumeDataArray, true);

      ClearCLBuffer lRGBABuffer =
                                lContext.createBuffer(MemAllocMode.AllocateHostPointer,
                                                      HostAccessType.ReadOnly,
                                                      KernelAccessType.WriteOnly,
                                                      4,
                                                      NativeTypeEnum.Byte,
                                                      Region2.region(l3DImage.getDimensions()));

      ImageRender lImageRender =
                               new ImageRender(lContext.getDefaultQueue(),
                                               Algorithm.MaximumProjection);

      lImageRender.render(l3DImage, lRGBABuffer, true);

      ClearCLImageViewer lView = ClearCLImageViewer.view(lRGBABuffer);

      while (lView.isShowing())
      {
        Thread.sleep(10);
      }

    }
  }

}
