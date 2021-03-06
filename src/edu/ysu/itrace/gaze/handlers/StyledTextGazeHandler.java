package edu.ysu.itrace.gaze.handlers;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.AstManager;
import edu.ysu.itrace.AstManager.SourceCodeEntity;
import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;

/**
 * Implements the gaze handler interface for a StyledText widget.
 */
public class StyledTextGazeHandler implements IGazeHandler {
    private IWorkbenchPartReference partRef;
    private StyledText targetStyledText;

    /**
     * Constructs a new gaze handler for the target StyledText object within the
     * workbench part specified by partRef.
     */
    public StyledTextGazeHandler(Object target, IWorkbenchPartReference partRef) {
        this.targetStyledText = (StyledText) target;
        this.partRef = partRef;
    }

    @Override
    public IStyledTextGazeResponse handleGaze(int absoluteX, int absoluteY,
            int relativeX, int relativeY, final Gaze gaze) {
        final int lineIndex;
        final int col;
        final Point absoluteLineAnchorPosition;
        final String name;
        final int lineHeight;
        final int fontHeight;
        final AstManager.SourceCodeEntity[] entities;
        final String path;

        try {
            AstManager astManager = (AstManager) targetStyledText
                    .getData(ControlView.KEY_AST);

            lineIndex = targetStyledText.getLineIndex(relativeY);
            int lineOffset = targetStyledText.getOffsetAtLine(lineIndex);
            int offset = targetStyledText.getOffsetAtLocation(new Point(
                    relativeX, relativeY));
            col = offset - lineOffset;

            // (0, 0) relative to the control in absolute screen
            // coordinates.
            Point relativeRoot = new Point(absoluteX - relativeX, absoluteY
                    - relativeY);
            // Top-left position of the first character on the line in
            // relative coordinates.
            Point lineAnchorPosition = targetStyledText
                    .getLocationAtOffset(targetStyledText
                            .getOffsetAtLine(lineIndex));
            // To absolute.
            absoluteLineAnchorPosition = new Point(lineAnchorPosition.x
                    + relativeRoot.x, lineAnchorPosition.y + relativeRoot.y);

            name = partRef.getPartName();
            lineHeight = targetStyledText.getLineHeight();
            fontHeight = targetStyledText.getFont().getFontData()[0]
                    .getHeight();
            entities = astManager.getSCEs(lineIndex + 1, col);
            path = astManager.getPath();
        } catch (IllegalArgumentException e) {
            /* An IllegalArgumentException SHOULD mean that the gaze fell
             * outside the valid text area, so just drop this one.
             */
            return null;
        }

        /*
         * This anonymous class just grabs the variables marked final
         * in the enclosing method and returns them.
         */
        return new IStyledTextGazeResponse() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getGazeType() {
                return "text";
            }

            @Override
            public int getLineHeight() {
                return lineHeight;
            }

            @Override
            public int getFontHeight() {
                return fontHeight;
            }

            @Override
            public Gaze getGaze() {
                return gaze;
            }

            public IGazeHandler getGazeHandler() {
                return StyledTextGazeHandler.this;
            }

            @Override
            public int getLine() {
                return lineIndex + 1;
            }

            @Override
            public int getCol() {
                return col;
            }

            // Write out the position at the top-left of the first
            // character in absolute screen coordinates.
            @Override
            public int getLineBaseX() {
                return absoluteLineAnchorPosition.x;
            }

            @Override
            public int getLineBaseY() {
                return absoluteLineAnchorPosition.y;
            }

            @Override
            public SourceCodeEntity[] getSCEs() {
                return entities;
            }

            @Override
            public String getPath() {
                return path;
            }

        };
    }
}
