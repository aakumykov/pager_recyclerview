package com.github.aakumykov.pager_recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PagerRecyclerView<ListItemType, ViewHolderType extends RecyclerView.ViewHolder>
        extends RecyclerView
        implements RecyclerView.OnChildAttachStateChangeListener
{
    private static final String TAG = PagerRecyclerView.class.getSimpleName();
    @Nullable private Page<ListItemType, ViewHolderType> mCurrentPage;
    @Nullable private Page<ListItemType, ViewHolderType> mNewPage;
    @Nullable private PageChangeCallback<ListItemType, ViewHolderType> mPageChangeCallback;
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


    protected abstract boolean areListItemsTheSame(@Nullable ListItemType firstItem,
                                                   @NonNull ListItemType secondItem);



    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {

        /* Этот метод вызывается не только при листании, но и при смене ViewHolder-а у элемента
        * (если используются разные VH для разных состояний элемента). Чтобы избежать
        * ложных сообщений о прикреплении первой страницы, используется нижеследующая проверка. */
        if (theSameItemIsRecreated(view))
            return;

        final Page<ListItemType, ViewHolderType> attachedPage = pageFromView(view);

        if (mFirstRun) {
            mFirstRun = false;
            mCurrentPage = attachedPage;
            reportFirstPageAttached();
        }
        else {
            mNewPage = attachedPage;
        }
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {

        final Page<ListItemType, ViewHolderType> detachedPage = pageFromView(view);

        if (newPageIs(detachedPage)) {
            mNewPage = null;
        }
        else if (currentPageIs(detachedPage) && null != mNewPage) {
            reportPageChanged(detachedPage, mNewPage);
            mCurrentPage = mNewPage;
            mNewPage = null;
        }
        else {
            Log.w(TAG, "onChildViewDetachedFromWindow(): неизвестный науке случай!");
        }
    }


    private void reportFirstPageAttached() {
        if (null != mPageChangeCallback)
            mPageChangeCallback.onFirstPageAttached(mCurrentPage);
    }

    private void reportPageChanged(final Page<ListItemType,ViewHolderType> oldPage,
                                   final Page<ListItemType,ViewHolderType> newPage) {
        if (null != mPageChangeCallback)
            mPageChangeCallback.onPageChanged(oldPage, newPage);
    }

    private boolean newPageIs(Page<ListItemType, ViewHolderType> detachedPage) {
        return null != mNewPage && detachedPage.viewHolder.equals(mNewPage.viewHolder);
    }

    private boolean currentPageIs(Page<ListItemType, ViewHolderType> detachedPage) {
        return null != mCurrentPage && areListItemsTheSame(mCurrentPage.listItem, detachedPage.listItem);
    }


    private Page<ListItemType, ViewHolderType> pageFromView(@NonNull View view) {
        final ViewHolderType attachedViewHolder = (ViewHolderType) view.getTag(R.id.key_view_holder);
        return new Page<>(itemFromView(view), attachedViewHolder);
    }

    private ListItemType itemFromView(@NonNull View view) {
        return (ListItemType) view.getTag(R.id.key_view_holder_payload);
    }

    private boolean theSameItemIsRecreated(@NonNull View view) {
        final ListItemType currentItem = getCurrentListItem();
        final ListItemType itemFromView = itemFromView(view);
        return areListItemsTheSame(currentItem, itemFromView);
    }

    private void subscribeToItself() {
        addOnChildAttachStateChangeListener(this);
    }



    public interface PageChangeCallback<ListItemType, ViewHolderType> {
        void onFirstPageAttached(final Page<ListItemType, ViewHolderType> page);
        void onPageChanged(final Page<ListItemType, ViewHolderType> oldPage,
                           final Page<ListItemType, ViewHolderType> newPage);
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


    // Методы для диагностики
    @Nullable
    protected ListItemType getCurrentListItem() {
        return (null != mCurrentPage) ? mCurrentPage.listItem : null;
    }
}
