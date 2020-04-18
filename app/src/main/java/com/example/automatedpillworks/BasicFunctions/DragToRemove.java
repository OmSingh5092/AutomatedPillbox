package com.example.automatedpillworks.BasicFunctions;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class DragToRemove extends ItemTouchHelper.SimpleCallback {
    public interface RecyclerItemDragListener{
        void onSwipe(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

    RecyclerItemDragListener listener;

    public DragToRemove(int dragDirs, int swipeDirs, RecyclerItemDragListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwipe(viewHolder, direction, viewHolder.getAdapterPosition());
    }
}
