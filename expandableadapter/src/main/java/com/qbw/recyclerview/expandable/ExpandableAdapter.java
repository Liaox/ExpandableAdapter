package com.qbw.recyclerview.expandable;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.qbw.log.XLog;
import com.qbw.recyclerview.base.BaseExpandableAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bond on 2016/4/2.
 */
public abstract class ExpandableAdapter<T> extends BaseExpandableAdapter<T> {
    private List<T> mList;
    private List<T> mHeaderList;
    private List<T> mChildList;
    private List<T> mGroupList;
    private Map<T, List<T>> mGroupChildMap;
    private List<T> mFooterList;

    public ExpandableAdapter(Context context) {
        super(context);
        mList = new ArrayList<>();
        mHeaderList = new ArrayList<>();
        mChildList = new ArrayList<>();
        mGroupList = new ArrayList<>();
        mGroupChildMap = new HashMap<>();
        mFooterList = new ArrayList<>();
    }


    @Override
    public T getItem(int adapPos) {
        return mList.get(adapPos);
    }

    public void removeItem(int adapPos) {
        int pos;
        int[] poss;
        if (-1 != (pos = getChildPosition(adapPos))) {
            removeChild(pos);
        } else if (-1 != (pos = getFooterPosition(adapPos))) {
            removeFooter(pos);
        } else if (-1 != (pos = getHeaderPosition(adapPos))) {
            removeHeader(pos);
        } else if (-1 != (poss = getGroupChildPosition(adapPos))[0]) {
            removeGroupChild(poss[0], poss[1]);
        } else if (-1 != (pos = getGroupPosition(adapPos))) {
            removeGroup(pos);
        } else {
            XLog.e("wrong adapter position[%d]", adapPos);
        }
    }

    public void updateItem(int adapPos, T t) {
        int pos;
        int[] poss;
        if (-1 != (pos = getChildPosition(adapPos))) {
            updateChild(pos, t);
        } else if (-1 != (pos = getFooterPosition(adapPos))) {
            updateFooter(pos, t);
        } else if (-1 != (pos = getHeaderPosition(adapPos))) {
            updateHeader(pos, t);
        } else if (-1 != (poss = getGroupChildPosition(adapPos))[0]) {
            updateGroupChild(poss[0], poss[1], t);
        } else if (-1 != (pos = getGroupPosition(adapPos))) {
            updateGroup(pos, t);
        } else {
            XLog.e("wrong adapter position[%d]", adapPos);
        }
    }

    public final void clear() {
        mList.clear();
        mHeaderList.clear();
        mChildList.clear();
        mGroupList.clear();
        mGroupChildMap.clear();
        mFooterList.clear();
        notifyDataSetChanged();
    }

    public final void addHeader(T t) {
        XLog.line(true);
        mHeaderList.add(t);
        int adapPos = convertHeaderPosition(getHeaderCount() - 1);
        mList.add(adapPos, t);
        notifyItemInserted(adapPos);
        XLog.line(false);
    }

    public final void addHeader(int headerPosition, T t) {
        XLog.line(true);
        if (checkHeaderPosition(headerPosition)) {
            mHeaderList.add(headerPosition, t);
            int adapPos = convertHeaderPosition(headerPosition);
            mList.add(adapPos, t);
            notifyItemInserted(adapPos);
        } else {
            addHeader(t);
        }
        XLog.line(false);
    }

    public final void addHeader(List<T> ts) {
        XLog.line(true);
        if (null == ts || ts.isEmpty()) {
            XLog.e("wrong param");
            return;
        }
        int oldHeaderCount = getHeaderCount();
        mHeaderList.addAll(ts);
        int adapPos = convertHeaderPosition(oldHeaderCount);
        mList.addAll(adapPos, ts);
        int addSize = ts.size();
        XLog.v("notify item from %d, count = %d", adapPos, addSize);
        notifyItemRangeInserted(adapPos, addSize);
        XLog.line(false);
    }

    public final void removeHeader(T t) {
        removeHeader(mHeaderList.indexOf(t));
    }

    public final T getHeader(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return null;
        }
        return mHeaderList.get(headerPosition);
    }

    public final void removeHeader(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertHeaderPosition(headerPosition);
        mHeaderList.remove(headerPosition);
        mList.remove(adapPos);
        notifyItemRemoved(adapPos);
        XLog.line(false);
    }

    public final void clearHeader() {
        clearHeader(0);
    }

    public final void clearHeader(int beginPos) {
        int headerCount = getHeaderCount();
        if (headerCount <= 0) {
            XLog.w("header count is 0");
            return;
        }
        XLog.line(true);
        if (beginPos < 0 || beginPos >= headerCount) {
            XLog.e("invalid begin pos[%d]", beginPos);
            return;
        }
        int adapPos = convertHeaderPosition(beginPos);
        mList.subList(adapPos, headerCount).clear();
        mHeaderList.subList(beginPos, headerCount).clear();
        notifyItemRangeRemoved(adapPos, headerCount - beginPos);
        XLog.line(false);
    }

    public final void updateHeader(int headerPosition, T t) {
        if (!checkHeaderPosition(headerPosition)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertHeaderPosition(headerPosition);
        mHeaderList.set(headerPosition, t);
        mList.set(adapPos, t);
        notifyItemChanged(headerPosition);
        XLog.line(false);
    }

    public final void notifyHeaderChanged(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertHeaderPosition(headerPosition);
        notifyItemChanged(adapPos);
        XLog.line(false);
    }

    @Override
    public final int getHeaderCount() {
        return null == mHeaderList ? 0 : mHeaderList.size();
    }

    private boolean checkHeaderPosition(int headerPos) {
        int headerCount;
        if (headerPos < 0) {
            XLog.e("invalid header position[%d]", headerPos);
            return false;
        } else if (headerPos >= (headerCount = getHeaderCount())) {
            XLog.e("invalid header position[%d], header size is [%d]", headerPos, headerCount);
            return false;
        }
        return true;
    }

    public final void addChild(T t) {
        XLog.line(true);
        int childCount = getChildCount();
        mChildList.add(t);
        int adapPos = convertChildPosition(childCount);
        mList.add(adapPos, t);
        notifyItemInserted(adapPos);
        XLog.line(false);
    }

    public final void addChildPosition(int childPos, T t) {
        XLog.line(true);
        int childCount = getChildCount();
        if (childPos >= childCount) {
            addChild(t);
        } else if (childPos >= 0) {
            mChildList.add(childPos, t);
            int adapPos = convertChildPosition(childPos);
            mList.add(adapPos, t);
            notifyItemInserted(adapPos);
        } else {
            XLog.e("wrong child position = %d", childPos);
        }
        XLog.line(false);
    }

    public final void addChild(List<T> tList) {
        if (null == tList || tList.isEmpty()) {
            XLog.e("wrong param tList");
            return;
        }

        XLog.line(true);
        int childCount = getChildCount();
        mChildList.addAll(tList);
        int adapPos = convertChildPosition(childCount);
        mList.addAll(adapPos, tList);
        int addSize = tList.size();
        XLog.v("notify chnaged item from %d, size = %d", adapPos, addSize);
        notifyItemRangeInserted(adapPos, addSize);
        XLog.line(false);
    }

    public final T getChild(int childPos) {
        if (!checkChildPosition(childPos)) {
            return null;
        }
        return mChildList.get(childPos);
    }

    public final void removeChild(int childPos) {
        if (!checkChildPosition(childPos)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertChildPosition(childPos);
        mChildList.remove(childPos);
        mList.remove(adapPos);
        notifyItemRemoved(adapPos);
        XLog.line(false);
    }

    public final void removeChild(int childPos, int count) {
        if (!checkChildPosition(childPos)) {
            return;
        }
        if (count <= 0 || childPos + count - 1 >= getChildCount()) {
            XLog.e("wrong count[%d]", count);
            return;
        }
        XLog.line(true);
        int adapPos = convertChildPosition(childPos);
        mChildList.subList(childPos, childPos + count - 1).clear();
        mList.subList(adapPos, adapPos + count - 1).clear();
        notifyItemRangeRemoved(adapPos, count);
        XLog.line(false);
    }

    public final void clearChild() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            XLog.w("child count is 0");
            return;
        }
        XLog.line(true);
        int adapBeginPos = convertChildPosition(0);
        mList.removeAll(mChildList);
        mChildList.clear();
        notifyItemRangeRemoved(adapBeginPos, childCount);
        XLog.line(false);
    }

    public final void updateChild(int childPos, T t) {
        if (!checkChildPosition(childPos)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertChildPosition(childPos);
        mChildList.set(childPos, t);
        mList.set(adapPos, t);
        notifyItemChanged(adapPos);
        XLog.line(false);
    }

    public final void notifyChildChanged(int childPos) {
        if (!checkChildPosition(childPos)) {
            return;
        }
        XLog.line(true);
        notifyItemChanged(convertChildPosition(childPos));
        XLog.line(false);
    }

    @Override
    public final int getChildCount() {
        return null == mChildList ? 0 : mChildList.size();
    }

    private boolean checkChildPosition(int childPos) {
        int childCount;
        if (childPos < 0) {
            XLog.e("invalid child position[%d]", childPos);
            return false;
        } else if (childPos >= (childCount = getChildCount())) {
            XLog.e("invalid child position[%d], child size is [%d]", childPos, childCount);
            return false;
        }
        return true;
    }

    public final void addGroup(T t) {
        XLog.line(true);
        mGroupList.add(t);
        int groupCount = getGroupCount();
        mGroupChildMap.put(t, new ArrayList<T>());
        XLog.v("groupCount=%d", groupCount);
        int adapPos = convertGroupPosition(groupCount - 1);
        mList.add(adapPos, t);
        notifyItemInserted(adapPos);
        XLog.line(false);
    }

    public final void removeGroup(T t) {
        removeGroup(mGroupList.indexOf(t));
    }

    public final T getGroup(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return null;
        }
        return mGroupList.get(groupPosition);
    }

    public final void removeGroup(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }

        XLog.line(true);
        int groupChildCount = getGroupChildCount(groupPosition);
        if (groupChildCount > 0) {
            T groupItem = mGroupList.get(groupPosition);
            List<T> childList = mGroupChildMap.get(groupItem);
            mList.removeAll(childList);
            mGroupChildMap.remove(groupItem);
        }

        int adapPos = convertGroupPosition(groupPosition);
        mGroupList.remove(groupPosition);
        mList.remove(adapPos);
        notifyItemRangeRemoved(adapPos, groupChildCount + 1);
        XLog.line(false);
    }

    public final void updateGroup(int groupPosition, T t) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }

        XLog.line(true);
        int adapPos = convertGroupPosition(groupPosition);
        mGroupList.set(groupPosition, t);
        mList.set(adapPos, t);
        notifyItemChanged(adapPos);
        XLog.line(false);
    }

    @Override
    public final int getGroupCount() {
        return null == mGroupList ? 0 : mGroupList.size();
    }

    private boolean checkGroupPosition(int groupPos) {
        int groupCount;
        if (groupPos < 0) {
            XLog.e("invalid group position[%d]", groupPos);
            return false;
        } else if (groupPos >= (groupCount = getGroupCount())) {
            XLog.e("invalid group position[%d], group size is %d", groupPos, groupCount);
            return false;
        }

        return true;
    }

    public final void addGroupChild(int groupPosition, T t) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }
        XLog.line(true);
        mGroupChildMap.get(mGroupList.get(groupPosition)).add(t);
        int adapPos = convertGroupChildPosition(groupPosition, getGroupChildCount(groupPosition) - 1);
        mList.add(adapPos, t);
        notifyItemInserted(adapPos);
        XLog.line(false);
    }

    public final void addGroupChildPosition(int groupPosition, int childPos, T t) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }
        XLog.line(true);
        int groupChildCount = getGroupChildCount(groupPosition);
        if (childPos >= groupChildCount) {
            addGroupChild(groupPosition, t);
        } else if (childPos >= 0) {
            mGroupChildMap.get(mGroupList.get(groupPosition)).add(childPos, t);
            int adapPos = convertGroupChildPosition(groupPosition, childPos);
            mList.add(adapPos, t);
            notifyItemInserted(adapPos);
        } else {
            XLog.e("wrong targetChildPos = %d", childPos);
        }
        XLog.line(false);
    }

    public final void addGroupChild(int groupPosition, List<T> childList) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }
        if (null == childList || childList.isEmpty()) {
            XLog.e("invalid child mList");
            return;
        }

        XLog.line(true);
        int oldGroupChildCount = getGroupChildCount(groupPosition);
        mGroupChildMap.get(mGroupList.get(groupPosition)).addAll(childList);
        int adapPos = convertGroupChildPosition(groupPosition, oldGroupChildCount);
        mList.addAll(adapPos, childList);
        notifyItemRangeInserted(adapPos, childList.size());
        XLog.line(false);
    }

    public final T getGroupChild(int groupPosition, int childPosition) {
        if (!checkGroupChildPosition(groupPosition, childPosition)) {
            return null;
        }
        return mGroupChildMap.get(mGroupList.get(groupPosition)).get(childPosition);
    }

    public final void removeGroupChild(int groupPosition, int childPosition) {
        if (!checkGroupChildPosition(groupPosition, childPosition)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertGroupChildPosition(groupPosition, childPosition);
        XLog.v("groupPosition=%d, childPosition=%d, adapGroupChildPos=%d", groupPosition, childPosition, adapPos);
        mGroupChildMap.get(mGroupList.get(groupPosition)).remove(childPosition);
        mList.remove(adapPos);
        notifyItemRemoved(adapPos);
        XLog.line(false);
    }

    public final void updateGroupChild(int groupPosition, int childPosition, T t) {
        if (!checkGroupChildPosition(groupPosition, childPosition)) {
            return;
        }
        XLog.line(true);
        int adapPos = convertGroupChildPosition(groupPosition, childPosition);
        XLog.v("groupPosition=%d, childPosition=%d, adapGroupChildPos=%d", groupPosition, childPosition, adapPos);
        mGroupChildMap.get(mGroupList.get(groupPosition)).set(childPosition, t);
        mList.set(adapPos, t);
        notifyItemChanged(adapPos);
        XLog.line(false);
    }

    @Override
    public final int getGroupChildCount(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return 0;
        }
        T groupItem = mGroupList.get(groupPosition);
        return mGroupChildMap.get(groupItem).size();
    }

    public final void notifyGroupChildChanged(int groupPosition, int childPosition) {
        if (!checkGroupChildPosition(groupPosition, childPosition)) {
            return;
        }
        int adapPos = convertGroupChildPosition(groupPosition, childPosition);
        notifyItemChanged(adapPos);
    }

    private boolean checkGroupChildPosition(int groupPos, int childPos) {
        int groupChildCount;
        if (childPos < 0) {
            XLog.e("invalid group child position[%d, %d]", groupPos, childPos);
            return false;
        } else if (!checkGroupPosition(groupPos)) {
            return false;
        } else if (childPos >= (groupChildCount = getGroupChildCount(groupPos))) {
            XLog.e("invalid group child position[%d, %d], group[%d] child size is [%d]", groupPos, childPos, groupPos, groupChildCount);
            return false;
        }
        return true;
    }

    public final void addFooter(T t) {
        XLog.line(true);
        mFooterList.add(t);
        int adapPos = convertFooterPosition(getFooterCount() - 1);
        mList.add(adapPos, t);
        notifyItemInserted(adapPos);
        XLog.line(false);
    }

    public final void addFooterPosition(int position, T t) {
        XLog.line(true);
        int footerCount = getFooterCount();
        if (position < 0) {
            XLog.e("Invalid position = %d", position);
            return;
        }
        if (position >= footerCount) {
            addFooter(t);
        } else {
            mFooterList.add(position, t);
            int adapPos = convertFooterPosition(position);
            mList.add(adapPos, t);
            notifyItemInserted(adapPos);
        }
        XLog.line(false);
    }

    public final void removeFooter(T t) {
        removeFooter(mFooterList.indexOf(t));
    }

    public final T getFooter(int footerPosition) {
        if (!checkFooterPosition(footerPosition)) {
            return null;
        }
        return mFooterList.get(footerPosition);
    }

    public final void removeFooter(int footerPosition) {
        if (!checkFooterPosition(footerPosition)) {
            return;
        }

        int adapPos = convertFooterPosition(footerPosition);
        mFooterList.remove(footerPosition);
        mList.remove(adapPos);
        notifyItemRemoved(adapPos);
    }

    public final void updateFooter(int footerPosition, T t) {
        if (!checkFooterPosition(footerPosition)) {
            return;
        }

        int adapPos = convertFooterPosition(footerPosition);
        mFooterList.set(footerPosition, t);
        mList.set(adapPos, t);
        notifyItemChanged(adapPos);
    }

    @Override
    public final int getFooterCount() {
        return null == mFooterList ? 0 : mFooterList.size();
    }

    private boolean checkFooterPosition(int footerPos) {
        int footerCount;
        if (footerPos < 0) {
            XLog.e("invalid footer position[%d]", footerPos);
            return false;
        } else if (footerPos >= (footerCount = getFooterCount())) {
            XLog.e("invalid footer position[%d], footer size is [%d]", footerPos, footerCount);
            return false;
        }
        return true;
    }

    public abstract RecyclerView.ViewHolder onCreateStickyGroupViewHolder(int groupPosition);

    public abstract void bindStickyGroupData(int groupPosition, RecyclerView.ViewHolder stickyGroupViewHolder);

    public abstract boolean isPositionHeader(int adapPos);

    public abstract boolean isPositionChild(int adapPos);

    public abstract boolean isPostionGroup(int adapPos);

    public abstract boolean isPostionGroupChild(int adapPos);

    public abstract boolean isPositionFooter(int adapPos);

    public abstract Point getGroupSize(int groupPosition);
}
