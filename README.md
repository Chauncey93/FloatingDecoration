##0.前言
FloatingDecoration本质上就是一个自定义ItemDecoration，FloatingDecoration的效果图如下：
![这里写图片描述](http://img.blog.csdn.net/20170110121141732?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##1.使用方法
FloatingDecoration提供了一个对外接口

```
    public interface DecorationCallback {
        String getGroupLabel(int position);
    }
```

初始化

```
//第一个参数为Context，第二个为DecorationCallback 。
FloatingDecoration decoration = new FloatingDecoration(this, this);
recyclerView.addItemDecoration(decoration);
//设置分隔线，参数为Drawable
decoration.setDividingLine(mDividingLine);
```


这里返回的字符串为分组的文本标识

##2.ItemDecoration
ItemDecoration类主要有三种方法：

```
public void onDraw(Canvas c, RecyclerView parent, State state)
public void onDrawOver(Canvas c, RecyclerView parent, State state)
public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state)
```
三种方法作用如下：
onDraw：绘制背景，会被Item的内容所覆盖。适合用来绘制每个分组的标识
onDrawOver：绘制前景，会覆盖Item的内容。适合用来绘制悬浮的顶部。
getItemOffsets：获取Item的距离，也可以给Item添加边距。绘制之前需要先空出一些空间。

##3.FloatingDecoration源码解析
1.getItemOffsets

```
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int pos = parent.getChildAdapterPosition(view);
        String groupLabel = mDecorationCallback.getGroupLabel(pos);
        if (groupLabel == null) return;
        //只有每个分组的第一个item需要label
        if (isFirstInGroup(pos))
            outRect.top = mLabelHeight;
    }
    
    private boolean isFirstInGroup(int pos) {
        if (pos == 0) {
            return true;
        } else {
            String prevLabel = mDecorationCallback.getGroupLabel(pos - 1);
            String label = mDecorationCallback.getGroupLabel(pos);
			//与上一个item的label进行对比，若不一致则当前item是新一组的第一个元素
            if (prevLabel.equals(label)) {
                return false;
            } else {
                return true;
            }
        }
    }
```
放两张设置前后的效果对比图
<img src="http://img.blog.csdn.net/20170105142638323?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="270" height="480"/> <img src="http://img.blog.csdn.net/20170105142702558?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="270" height="480"/>

2.onDraw

```
	//绘制分组的label
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        if (mDividingLine != null) {
        //绘制分隔线。ps：可通过setDividingLine(Drawable drawable)设置分隔线
            drawDividingLine(c, parent, left, right, childCount);
        }
        //绘制分组标识
        drawLabelText(c, parent, left, right, childCount);
    }

    private void drawDividingLine(Canvas canvas, RecyclerView parent, int left, int right, int childCount) {
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int top = view.getBottom() + lp.bottomMargin;
            int bottom = top + mDividingLine.getIntrinsicHeight();
            mDividingLine.setBounds(left, top, right, bottom);
            mDividingLine.draw(canvas);
        }
    }

    private void drawLabelText(Canvas canvas, RecyclerView parent, int left, int right, int childCount) {
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            if (isFirstInGroup(position)) {
                String groupLabel = mDecorationCallback.getGroupLabel(position);
                if (groupLabel == null) return;
                int bottom = view.getTop();
                int top = bottom  - mLabelHeight;
                //绘制矩形
                canvas.drawRect(left, top, right, bottom, mPaint);
                //将文本绘制在矩形的竖直中间位置
                canvas.drawText(groupLabel , left + 30, bottom - mLabelHeight / 2 + (float) getLabelHeight() / 4, mTextPaint);
            }
        }
    }
```

```
	//获取label文本的高度
	private Paint.FontMetrics mMetrics = mTextPaint.getFontMetrics();
    private double getLabelHeight() {
        return Math.ceil(mMetrics.bottom - mMetrics.top);
    }
```
设置之后整体效果如下
<img src="http://img.blog.csdn.net/20170105145151490?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="270" height="480"/>

3.onDrawOver

```
@Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        //获取当前可见的第一个item，意思就是在当前屏幕中的列表的第一个可见item。（已经划出屏幕外的item不算）
        int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        String label = mDecorationCallback.getGroupLabel(position);
        canvas.drawRect(left, 0, right, mLabelHeight, mPaint);
        canvas.drawText(label, 30, mLabelHeight / 2 + (float) getLabelHeight() / 4, mTextPaint);
    }
```
效果如图：
![这里写图片描述](http://img.blog.csdn.net/20170110121212357?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
不过可以看得出来，顶部label的切换很突兀，于是想做一个慢慢把上一个label顶出屏幕的效果。

思路大致如下：

正常情况下悬浮顶部一直绘制在最上方
![这里写图片描述](http://img.blog.csdn.net/20170105154618243?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

当分组的最后一个Item离开屏幕时，该分组的Label也随之跟着离开，而此时下面又接着下一个分组的label，就会产生下个label把上个label顶出的效果：

![这里写图片描述](http://img.blog.csdn.net/20170105155340520?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


把代码改成：

```
	//绘制悬浮顶部
    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        String label = mDecorationCallback.getGroupLabel(position);
        //此处开始不同
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            //判断item是不是该组的最后一个元素
            if (isLastInGroup(position)) {
                int bottom = child.getBottom();
                //滑动过程中，若分组中最后一个item的bottom小于label的高度，就把label的绘制位置往上提。
                if (bottom <= mLabelHeight) {
                    canvas.drawRect(left, 0, right, bottom, mPaint);
                    canvas.drawText(label, 30, mLabelHeight / 2 + (float) getLabelHeight() / 4 - (mLabelHeight - bottom), mTextPaint);
                    //以下代码不再执行，免得出现两次绘制。
                    return;
                }
            }
        }
        canvas.drawRect(left, 0, right, mLabelHeight, mPaint);
        canvas.drawText(label, 30, mLabelHeight / 2 + (float) getLabelHeight() / 4, mTextPaint);
    }


    private boolean isLastInGroup(int pos) {

        String label = mDecorationCallback.getGroupLabel(pos);
        String nextLabel;
        try {
	        //若item是列表中所有item的最后一个，则pos+1会导致角标越界
            nextLabel = mDecorationCallback.getGroupLabel(pos + 1);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return true;
        }

        if (!label.equals(nextLabel)) return true;

        return false;
    }
```
大功告成，再来看看效果：
![这里写图片描述](http://img.blog.csdn.net/20170110121141732?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3gxMjI5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
源码已经上传至[Github](https://github.com/Chauncey93/FloatingDecoration)，喜欢的话记得给我点个star。