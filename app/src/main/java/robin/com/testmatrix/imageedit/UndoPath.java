package robin.com.testmatrix.imageedit;

import android.graphics.Path;

/**
 * Created by Robin Yang on 12/7/17.
 */

public class UndoPath implements Undoable {

    float strokeWidth;  // 画笔大小
    int color;        // 画笔颜色
    Path path;          // 画笔的路径

    public UndoPath(float strokeWidth, int color, Path path) {
        this.strokeWidth = strokeWidth;
        this.color = color;
        this.path = path;
    }
}
