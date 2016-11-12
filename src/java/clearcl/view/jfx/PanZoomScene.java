package clearcl.view.jfx;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * This subclass of Scene allows for panning and zooming.
 *
 * @author royer
 */
public class PanZoomScene extends Scene
{
  private Node mPanZoomNode;
  private volatile double mPressedX, mPressedY;
  private Scale mScale;
  private Translate mTranslate;
  private Point2D mSceneCenterInRoot;
  private boolean mPivotInitialized;

  /**
   * Constructs a PanZoomScene from a root node, a node to pan and zoom, window
   * dimensions and a fill color.
   * 
   * @param pRoot
   *          root node
   * @param pNodeToPanZoom
   *          node to pan and zoom
   * @param pWidth
   *          window width
   * @param pHeight
   *          window height
   * @param pFill
   *          fill color
   */
  public PanZoomScene(Parent pRoot,
                      Node pNodeToPanZoom,
                      double pWidth,
                      double pHeight,
                      Paint pFill)
  {
    super(pRoot, pWidth, pHeight, pFill);
    mPanZoomNode = pNodeToPanZoom;

    mScale = new Scale();
    mTranslate = new Translate();
    mPanZoomNode.getTransforms().add(mScale);
    mPanZoomNode.getTransforms().add(mTranslate);

    setOnMousePressed((event) -> {

      if (event.getButton() == MouseButton.PRIMARY)
      {
        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getWidth();
        double lSceneHeight = getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11
            && lMouseY >= 10
            && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(event.getX(),
                                                             event.getY());
          mPressedX = lRootNodePoint.getX();
          mPressedY = lRootNodePoint.getY();
          event.consume();
        }
      }
    });

    setOnMouseDragged((event) -> {
      if (event.getButton() == MouseButton.PRIMARY)
      {
        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getWidth();
        double lSceneHeight = getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11
            && lMouseY >= 10
            && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(lMouseX,
                                                             lMouseY);

          double lDeltaX = mTranslate.getX() + (lRootNodePoint.getX() - mPressedX);
          double lDeltaY = mTranslate.getY() + (lRootNodePoint.getY() - mPressedY);

          mTranslate.setX(lDeltaX);
          mTranslate.setY(lDeltaY);

          event.consume();
        }
      }
    });

    setOnScroll((event) -> {

      double lDelta = event.getDeltaY() * 0.001;
      double lDeltaFactor = Math.exp(lDelta);

      scaleScene(lDeltaFactor);

      if (!mPivotInitialized)
      {
        resetZoomPivot();
        mPivotInitialized = true;
      }

      event.consume();
    });

    widthProperty().addListener((obs, o, n) -> {
      resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
    });

    heightProperty().addListener((obs, o, n) -> {
      resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
    });

  }

  private void scaleScene(double lDeltaFactor)
  {
    if (lDeltaFactor < 0.5)
      lDeltaFactor = 0.5;
    else if (lDeltaFactor > 2)
      lDeltaFactor = 2;

    mScale.setX(mScale.getX() * lDeltaFactor);
    mScale.setY(mScale.getY() * lDeltaFactor);
  }

  private void resetZoomPivot()
  {
    double lSceneWidth = getWidth();
    double lSceneHeight = getHeight();

    mScale.setPivotX(lSceneWidth / 2);
    mScale.setPivotY(lSceneHeight / 2);/**/
  }

}