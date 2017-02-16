package vandy.mooc.uniqueidgen.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

import vandy.mooc.uniqueidgen.R;
import vandy.mooc.uniqueidgen.services.UniqueIDGenService;

/**
 * A custom view class that animates a circle along path view widgets that
 * connect activity, service, and 4 thread widgets.
 */
public class GeneratorView extends ConstraintLayout {
    /**
     * Constants ids used to identify path end points. Thread end points are
     * dynamically specified by using the Android thread id.
     */
    public static final int START_NODE = 1;
    public static final int ACTIVITY_NODE = 2;
    public static final int SERVICE_NODE = 3;
    public static final int END_NODE = 4;
    /**
     * Diameter of animated circle
     */
    private static final int mDiameter = 20;
    /**
     * Default animation duration between two nodes.
     */
    private static int DURATION = 400;
    /**
     * Used to generate unique ids for each animation
     */
    private static int mUniqueId = 0;
    /**
     * Paint instance used for background
     */
    private Paint mPaint = new Paint();
    /**
     * Paint instance used for drawing a circle
     */
    private Paint mPaintCircle = new Paint();
    /**
     * Map containing all currently running animators
     */
    private ArrayMap<Integer, PathAnimator> mAnimators;
    /**
     * Maps thread ids to each thread arrow widget
     */
    private ArrayMap<Integer, Integer> mThreadToIdMap;
    /**
     * Keeps track of the number currently running thread animations.
     */
    private int mThreadCount = 0;
    /**
     * Static list of 4 thread arrow widgets
     */
    private int[] mThreadNodeIds = new int[]{
            R.id.thread1_path_view,
            R.id.thread2_path_view,
            R.id.thread3_path_view,
            R.id.thread4_path_view};

    public GeneratorView(Context context) {
        super(context);
        init(null, 0);
    }

    public GeneratorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GeneratorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Construction helper.
     *
     * @param attrs    array of custom attributes
     * @param defStyle default style
     */
    private void init(AttributeSet attrs, int defStyle) {
        // This is necessary for enable onDraw() calls for a ViewGroup.
        setWillNotDraw(false);
        mPaint.setColor(Color.WHITE);
        mPaintCircle.setColor(Color.BLUE);
        mAnimators = new ArrayMap<>();
        mThreadToIdMap = new ArrayMap<>(4);
    }

    /**
     * Hook method called on each frame cycle.
     *
     * @param canvas the canvas in which to draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw each running animation at their specified offsets.
        mAnimators.forEach((integer, pathAnimator) -> {
            View view = pathAnimator.getPathView();
            if (view != null) {
                int radius = view.getWidth() / 2;
                int horizontalOffset =
                        view.getLeft() + (int) (view.getWidth() / 2.0);
                canvas.drawCircle(horizontalOffset,
                                  view.getBottom()
                                          + pathAnimator.getVerticalOffset(),
                                  radius,
                                  mPaintCircle);
            }
        });

    }

    /**
     * Shows or hides a indeterminate progressBar to indicate if a thread is, or
     * is not, currently running.
     *
     * @param threadId The id of the thread.
     * @param busy     {@true} to show progressBar, {@code false} to hide it.
     */
    private void setThreadBusy(int threadId, boolean busy) {
        int id = mThreadToIdMap.get(threadId);
        int progressBarId;
        int textViewId;
        switch (id) {
            case R.id.thread1_path_view:
                progressBarId = R.id.thread1_progressBar;
                textViewId = R.id.thread1_text_view;
                break;
            case R.id.thread2_path_view:
                progressBarId = R.id.thread2_progressBar;
                textViewId = R.id.thread2_text_view;
                break;
            case R.id.thread3_path_view:
                progressBarId = R.id.thread3_progressBar;
                textViewId = R.id.thread3_text_view;
                break;
            case R.id.thread4_path_view:
                progressBarId = R.id.thread4_progressBar;
                textViewId = R.id.thread4_text_view;
                break;
            default:
                throw new IllegalArgumentException(
                        "ID " + id + " is not a valid thread path resource id");
        }
        findViewById(progressBarId).setVisibility(busy ? VISIBLE : INVISIBLE);
        findViewById(textViewId).setVisibility(busy ? INVISIBLE : VISIBLE);
    }

    /**
     * (Not currently used) The animation duration for each segment is scaled so
     * that the animation moves at the same speed no matter how long the path
     * is.
     *
     * @param distance path length (pixels).
     * @return A scaled distance.
     */
    private int calcScaledDuration(int distance) {
        return (int) (((float) DURATION / 200.0) * distance);
    }

    /**
     * Creates a new animation instance and immediately begins an animation
     * along the specified node path.
     *
     * @param nodes    An ordered node path to animate.
     * @param callback An optional callback invoked at the end of each
     *                 animation.
     * @return A unique id for this animation.
     */
    public int startAnimation(AnimationCallback callback, int... nodes) {
        PathAnimator pathAnimator = new PathAnimator(callback, nodes);
        mAnimators.put(pathAnimator.getId(), pathAnimator);
        return pathAnimator.getId();
    }

    /**
     * Ends the animation associated with the passed id and also removes the
     * PathAnimation instance from the list of running animations.
     *
     * @param requestId The id of the PathAnimator to end.
     */
    public void endAnimation(int requestId) {
        PathAnimator pathAnimator = mAnimators.remove(requestId);
        if (pathAnimator != null) {
            pathAnimator.terminate();
        }
    }

    /**
     * Appends the specified node path to the PathAnimator identified by the
     * passed id. The animation is automatically run after the new node path is
     * appended.
     *
     * @param id    The id of the PathAnimator instance to update.
     * @param nodes An ordered node path to animate.
     */
    public void addAnimation(int id, AnimationCallback callback, int... nodes) {
        PathAnimator pathAnimator = mAnimators.get(id);
        if (pathAnimator == null) {
            throw new IllegalArgumentException(
                    "Animator id " + id + " does not exist");
        }

        pathAnimator.appendPath(callback, nodes);
    }

    /**
     * Callback to be invoked when animation reaches the END node.
     */
    public interface AnimationCallback {
        void onAnimationEnd(int node);
    }

    /**
     * An instances of this class is created in response to each
     * startAnimation() request and all instances are maintained in mAnimations
     * ArrayMap. Each instance is removed from this map once the final node
     * (END_NODE) is reached.
     */
    private class PathAnimator {
        /**
         * Unique id of this instance.
         **/
        private int mId;
        /**
         * Ordered array of all path nodes
         **/
        private ArrayList<Integer> mPath;
        /**
         * The current animation position in the mPath node array
         **/
        private int mIndex;
        /**
         * The vertical offset to be used on the next draw() call
         **/
        private float mVerticalOffset;
        /**
         * A value animator which interpolates a vertical offset between the
         * current node and the next node in the path.
         */
        private ValueAnimator mAnimator;
        /**
         * A reference to the arrow vector drawable widget connects the current
         * node and the next node int the path.
         */
        private View mPathView;
        /**
         * An optional callback hook that will be invoked at the end of each
         * animation (node visit)
         */
        private AnimationCallback mCallback;

        /**
         * Creates a new instance with the specified node path and immediately
         * starts the animation.
         *
         * @param nodes the initial node path to animate
         */
        public PathAnimator(AnimationCallback callback, int... nodes) {
            mPath = new ArrayList<>();
            appendPath(callback, nodes);
            mId = ++mUniqueId;
        }

        /**
         * The unique instance id.
         *
         * @return a unique int value.
         */
        public int getId() {
            return mId;
        }

        /**
         * The current animated vertical offset to use when drawing the circle
         * shape on the next draw() call.
         *
         * @return a positive or negative offset distance from the start node
         */
        public float getVerticalOffset() {
            return mVerticalOffset;
        }

        /**
         * The vector drawable arrow view is stored to increase draw speeds.
         *
         * @return a reference to the arrow widget that is currently being
         * animated.
         */
        public View getPathView() {
            return mPathView;
        }

        /**
         * Appends a new path element to the end of the current animation path.
         * If the animation is currently running, it will automatically include
         * the added nodes in the animation. If it is not currently running, it
         * will be automatically started.
         *
         * @param nodes list of nodes to append to the current path
         */
        public void appendPath(AnimationCallback callback, int... nodes) {
            // Update callback
            mCallback = callback;

            // Append the passed nodes to the current path.
            Arrays.stream(nodes).forEach(node -> {
                if (mPath.size() > 0) {
                    if (mPath.get(mPath.size() - 1) == node) {
                        throw new IllegalArgumentException(
                                "LOOP detected when adding node " + node);
                    }
                }
                mPath.add(node);
            });

            // Start the animation if it is not already running.
            if (mAnimator == null || !mAnimator.isRunning()) {
                runAnimation();
            }
        }

        /**
         * Calculates the next arrow widget id that connects the current node
         * and the next node in the path list.
         *
         * @param consume {@code true} to increment the current node index, or
         *                {@code false) to leave the index unchanged.
         * @return resource id of the arrow widget connecting the current and
         * next node
         */
        private int getNextPathId(boolean consume) {
            int from = mPath.get(mIndex);
            int to = mPath.get(mIndex + 1);

            // If consume is specified, the automatically move
            // index marker to next node.
            if (consume) {
                mIndex++;
            }

            switch (from) {
                case START_NODE:
                    if (to == ACTIVITY_NODE) {
                        return R.id.play_path_view;
                    } else {
                        throw new IllegalStateException(
                                "only Start --> Activity is supported");
                    }
                case ACTIVITY_NODE:
                    if (to == SERVICE_NODE) {
                        return R.id.input_path_view;
                    } else if (to == END_NODE) {
                        return R.id.guid_path_view;
                    } else {
                        throw new IllegalStateException(
                                "only Activity --> Service and Activity --> "
                                        + "END is supported");
                    }
                case SERVICE_NODE:
                    switch (to) {
                        case ACTIVITY_NODE:
                            return R.id.output_path_view;
                        default:
                            // Thread nodes are identified by their
                            // unique thread ids.
                            if (mThreadToIdMap.containsKey(to)) {
                                return mThreadToIdMap.get(to);
                            } else if (mThreadCount < mThreadNodeIds.length) {
                                int pathId = mThreadNodeIds[mThreadCount++];
                                mThreadToIdMap.put(to, pathId);
                                return pathId;
                            } else {
                                throw new IllegalStateException(
                                        "A maximum of 4 "
                                                + mThreadNodeIds.length
                                                + " thread nodes are "
                                                + "supported");
                            }
                    }
                case END_NODE:
                    throw new IllegalStateException("END --> is not supported");

                default:
                    if (to == SERVICE_NODE) {
                        // Thread nodes are identified by their
                        // unique thread ids.
                        if (mThreadToIdMap.containsKey(from)) {
                            return mThreadToIdMap.get(from);
                        } else {
                            if (mThreadCount < UniqueIDGenService.MAX_THREADS) {
                                int pathId = mThreadNodeIds[mThreadCount++];
                                mThreadToIdMap.put(from, pathId);
                                return pathId;
                            } else {
                                throw new IllegalStateException(
                                        "Maximum of  " + mThreadCount
                                                + "threads supported");

                            }
                        }
                    } else {
                        throw new IllegalStateException(
                                "only Thread<N> --> Service is supported");
                    }
            }
        }

        /**
         * Determines the offset direction (positive or negative) of the path
         * between the current node and then next node in the path.
         *
         * @return -1 if arrow direction is upward, or 1 if arrow direction is
         * downward
         */
        public int getNextDirection() {
            int from = mPath.get(mIndex);
            int to = mPath.get(mIndex + 1);

            switch (from) {
                case START_NODE:
                case ACTIVITY_NODE:
                    return -1;
                case SERVICE_NODE:
                    return to == ACTIVITY_NODE ? 1 : -1;
                default:
                    // Must be from thread (to service)
                    return 1;
            }
        }

        /**
         * Runs a single ValueAnimator to draw a small filled circle that moves
         * between the current and next node in the node path.
         */
        public void runAnimation() {
            // Lookup the path view widget id that links the next two nodes.
            final int from = mPath.get(mIndex);
            final int to = mPath.get(mIndex + 1);
            final int dir = getNextDirection();
            final int id = getNextPathId(true);

            View pathView = findViewById(id);
            final Rect rect = new Rect();
            pathView.getDrawingRect(rect);
            int distance = rect.height() + mDiameter;

            if (dir < 0) {
                mAnimator = ValueAnimator.ofFloat(0, -distance);
            } else {
                mAnimator = ValueAnimator.ofFloat(-distance, 0);
            }

            // If we are animating from a thread back to the service
            // then hide the progress view for the thread.
            if (from > END_NODE) {
                setThreadBusy(from, false);
            }

            mAnimator.setInterpolator(new LinearInterpolator());

            // The path from ACTIVITY_NODE to END_NODE is much longer
            // than the other paths, so speed up the duration to produce
            // a more pleasing overall effect.
            mAnimator.setDuration(to == END_NODE ? DURATION * 2 : DURATION);

            // Install an animation listener.
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // If we just animated to a thread view then
                    // show the progress animation.
                    if (to > END_NODE) {
                        setThreadBusy(to, true);
                    }

                    // Check if there are more nodes to visit.
                    if (mIndex < mPath.size() - 1) {
                        // Process the next animation in the sequence.
                        runAnimation();
                    } else {
                        // If a callback has been specified, call it now that
                        // all paths have been animated.
                        if (mCallback != null) {
                            mCallback.onAnimationEnd(to);
                        }
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }
            });

            // Install a listener to force repaints as the value animator
            // interpolates the vertical distance between the current and
            // next node.
            mAnimator.addUpdateListener(animation -> {
                mVerticalOffset = (float) animation.getAnimatedValue();
                mPathView = findViewById(id);
                invalidate();
            });

            // Start the animation and return. This is a non-blocking call.
            mAnimator.start();
        }

        /**
         * Ends the current animation.
         */
        public void terminate() {
            if (mAnimator != null) {
                mAnimator.end();
            }
        }
    }
}
