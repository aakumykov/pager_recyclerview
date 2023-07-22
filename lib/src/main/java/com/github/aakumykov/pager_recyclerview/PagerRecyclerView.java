package com.github.aakumykov.pager_recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.BiFunction;

public class PagerRecyclerView<ListItemType, ViewHolderType extends RecyclerView.ViewHolder>
        extends RecyclerView
        implements RecyclerView.OnChildAttachStateChangeListener
{
    @Nullable private Page<ListItemType, ViewHolderType> mCurrentPage;
    @Nullable private Page<ListItemType, ViewHolderType> mNewPage;
    @Nullable private PageChangeCallback<ListItemType, ViewHolderType> mPageChangeCallback;
    @Nullable private BiFunction<ListItemType, ListItemType, Boolean> mItemsComparator;
    private boolean mFirstRun = true;


    public PagerRecyclerView(@NonNull Context context) {
        super(context);
        subscribeToItself();
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        subscribeToItself();
    }

    public PagerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        subscribeToItself();
    }


    public void setPageChangeCallback(@Nullable PageChangeCallback<ListItemType, ViewHolderType> pageChangeCallback) {
        mPageChangeCallback = pageChangeCallback;
    }

    public void unsetPageChangeCallback() {
        mPageChangeCallback = null;
    }


    public void setItemsComparator(@NonNull BiFunction<ListItemType, ListItemType, Boolean> comparator) {
        mItemsComparator = comparator;
    }


    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {

        final Page<ListItemType, ViewHolderType> attachedPage = pageFromView(view);

        if (null == mCurrentPage)
            mCurrentPage = attachedPage;
        else
            mNewPage = attachedPage;

        reportPageAttached(attachedPage);
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {

        final Page<ListItemType, ViewHolderType> detachedPage = pageFromView(view);

        if (currentPageIs(detachedPage)) {
            mCurrentPage = mNewPage;
            mNewPage = null;

            reportPageDetached(detachedPage);
            reportPageChanged(detachedPage, mCurrentPage);
        }
        else if (newPageIs(detachedPage)) {
            reportPageDetached(mNewPage);
            mNewPage = null;
        }
        /*else
            throw new IllegalStateException("Нарушена логика метода: ни currentItem, ни newItem не совпадают с detachedItem:\n" +
                    "currentItem: "+ mCurrentPage +"\n" +
                    "newItem: "+ mNewPage +"\n" +
                    "detachedPage: "+detachedPage);*/
    }

    private void reportPageAttached(final Page<ListItemType,ViewHolderType> attachedPage) {
        if (null != mPageChangeCallback) {
            if (mFirstRun) {
                mFirstRun = false;
                mPageChangeCallback.onFirstPageAttached(attachedPage);
            }
            mPageChangeCallback.onNewPageAttached(attachedPage);
        }
    }

    private void reportPageDetached(final Page<ListItemType,ViewHolderType> detachedPage) {
        if (null != mPageChangeCallback)
            mPageChangeCallback.onOldPageDetached(detachedPage);
    }

    private void reportPageChanged(final Page<ListItemType,ViewHolderType> oldPage,
                                   final Page<ListItemType,ViewHolderType> newPage) {
        if (null != mPageChangeCallback)
            mPageChangeCallback.onPageChanged(oldPage, newPage);
    }

    private void subscribeToItself() {
        addOnChildAttachStateChangeListener(this);
    }

    private boolean newPageIs(Page<ListItemType, ViewHolderType> detachedPage) {
        return null != mNewPage && detachedPage.viewHolder.equals(mNewPage.viewHolder);
    }

    private boolean currentPageIs(Page<ListItemType, ViewHolderType> detachedPage) {
        if (null == mItemsComparator)
            throw new IllegalStateException("Item comparator function must be set with setItemsComparator() method.");

        return (null != mCurrentPage &&
                mItemsComparator.apply(mCurrentPage.listItem, detachedPage.listItem));
    }


    private Page<ListItemType, ViewHolderType> pageFromView(@NonNull View view) {
        final ListItemType attachedItem = (ListItemType) view.getTag(R.id.key_view_holder_payload);
        final ViewHolderType attachedViewHolder = (ViewHolderType) view.getTag(R.id.key_view_holder);
        return new Page<>(attachedItem, attachedViewHolder);
    }



    public interface PageChangeCallback<ListItemType, ViewHolderType> {

        void onFirstPageAttached(final Page<ListItemType, ViewHolderType> page);

        void onPageChanged(final Page<ListItemType, ViewHolderType> oldPage,
                           final Page<ListItemType, ViewHolderType> newPage);

        default void onNewPageAttached(final Page<ListItemType, ViewHolderType> newPage) {}

        default void onOldPageDetached(final Page<ListItemType, ViewHolderType> oldPage) {}
    }


    public static class Page<ListItemType, ViewHolderType> {

        public final ListItemType listItem;
        public final ViewHolderType viewHolder;

        public Page(ListItemType listItem, ViewHolderType viewHolder) {
            this.listItem = listItem;
            this.viewHolder = viewHolder;
        }

        @NonNull @Override
        public String toString() {
            return "Page{" +
                    "listItem=" + listItem +
                    ", viewHolder=" + viewHolder +
                    '}';
        }
    }

}
