package com.example.notesapp.entities;

import android.view.View;

public interface Onclicklistener {
   void OnfileClicked( View view ,Note note ,int position) ;
   void OnLongClick (View view, Note note, int position) ;

}
