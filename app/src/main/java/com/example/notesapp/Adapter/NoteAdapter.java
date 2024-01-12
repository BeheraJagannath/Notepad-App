package com.example.notesapp.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.notesapp.R;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.Onclicklistener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>  {
        private Onclicklistener onclicklistener;
        private List<Note> notes ;
        private Activity activity ;

        private List<Note> notesource ;
        private Timer timer ;
        public SparseBooleanArray mSelectedItemsIds;




        public NoteAdapter(Activity activity , List<Note> notes, Onclicklistener onclicklistener){
            this.notes = notes ;
            this.onclicklistener = onclicklistener ;
            this.notesource = notes ;
            this.activity = activity ;
            mSelectedItemsIds = new SparseBooleanArray();


        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from ( parent.getContext()).inflate(R.layout.notes_layout,parent,false ));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.setnote (notes.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onclicklistener.OnfileClicked(holder.itemView, notes.get(position), position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onclicklistener.OnLongClick(holder.itemView,notes.get(position) ,position);

                    return true;
                }
            });


        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }


    public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name ,subtitle , note , datetime ;
            LinearLayout lin ;
            RoundedImageView image_note ;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                subtitle = itemView.findViewById(R.id.subtitle);
                datetime = itemView.findViewById(R.id.datetime);
                note = itemView.findViewById(R.id.note);
                lin = itemView.findViewById(R.id.lin);
                image_note = itemView.findViewById(R.id.image_note);
            }

            public void setnote(Note notes) {
                name .setText(notes.getTitle());
                note .setText( notes.getNoteText());
                if (notes.getSubtitle().trim().isEmpty()){
                    subtitle.setVisibility( View.GONE);

                }else {
                    subtitle .setText(notes.getSubtitle());
                }
                datetime .setText(notes.getDatetime());
                GradientDrawable gradientDrawable = (GradientDrawable) lin .getBackground();
                if (notes. getColor()!=null){
                    gradientDrawable.setColor(Color.parseColor(notes.getColor()));
                }else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                if (notes.getImagePath() != null && !notes.getImagePath().isEmpty() ){
                    image_note.setVisibility(View.VISIBLE);
                    image_note.setImageBitmap(BitmapFactory.decodeFile(notes.getImagePath()));

                }else {
                    image_note.setVisibility(View.GONE);
                }



            }
        }
        public void searchNote(final String searchKeyword){
            timer = new Timer() ;
             timer.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     if (searchKeyword .isEmpty()){
                         notes = notesource ;


                     }else {
                         ArrayList<Note>temp = new ArrayList<>();
                         for (Note note:notesource){
                             if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                 || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                 ||note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){
                                 temp.add(note) ;
                             }
                         }
                         notes = temp ;

                     }
                     new Handler(Looper.getMainLooper()).post(new Runnable() {
                         @Override
                         public void run() {
                             notifyDataSetChanged();

                         }
                     });

                 }
             },500);

        }
        public void cancelTimer(){
            if (timer!=null){
                timer.cancel();
            }
        }

     public void toggleSelection(int position) {
      selectView(position, !mSelectedItemsIds.get(position));
     }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public void updateData(List<Note> viewModels) {
        notes.clear();
        notes.addAll(viewModels);
        notifyDataSetChanged();
    }




}
