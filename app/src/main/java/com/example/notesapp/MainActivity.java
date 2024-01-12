package com.example.notesapp;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.Adapter.NoteAdapter;
import com.example.notesapp.Database.NotesDatabase;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.Onclicklistener;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Onclicklistener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;
    FloatingActionButton noteadd;
    RecyclerView recyclerView;
    private List<Note> notelist;
    NoteAdapter noteadapter;
    Toolbar toolbar;
    private int noteClickPosition = -1;

    TextView no_data;
    private SparseBooleanArray selected;
    private int i;
    private Dialog dialog ;
    AdView adView ;
    private ColorDrawable background;
    private   InterstitialAd minterstitialAd ;



    private androidx.appcompat.view.ActionMode mActionMode;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteadd = findViewById(R.id.note_add);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        adView = findViewById(R.id.adView);
        no_data = findViewById(R.id.no_data);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        loadInterstitialAd();

        setSupportActionBar(toolbar);
//        toolbar.startActionMode(mActionMode);
        SetAdapter();
        loadExitDialog();
        getnotes(REQUEST_CODE_SHOW_NOTE, false);


        noteadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE);


            }
        });
        
    }


    private void SetAdapter() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        notelist = new ArrayList<>();
        noteadapter = new NoteAdapter(this, notelist, this);
        recyclerView.setAdapter(noteadapter);
        Collections.reverse(notelist);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitem, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                noteadapter.cancelTimer();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (notelist.size() != 0) {
                    noteadapter.searchNote(newText);
                }


                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE);

                return true;
            case R.id.delete_all:

                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).notedao().deleteAll();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);


                        noteadapter.updateData(notelist);

                        if (notelist.size() != 0) {
                            no_data.setVisibility(View.GONE);
                        } else {
                            no_data.setVisibility(View.VISIBLE);

                        }

                    }

                }
                new DeleteNoteTask().execute();


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void getnotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .notedao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                try {

                    if (requestCode == REQUEST_CODE_SHOW_NOTE) {
                        notelist.addAll(notes);
                        noteadapter.notifyDataSetChanged();


                    } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                        notelist.add(0, notes.get(0));
                        noteadapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);

                    } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                        notelist.remove(noteClickPosition);
                        if (isNoteDeleted) {
                            noteadapter.notifyItemRemoved(noteClickPosition);
                        } else {
                            notelist.add(noteClickPosition, notes.get(noteClickPosition));
                            noteadapter.notifyItemChanged(noteClickPosition);
                        }

                    }

                }catch (Exception e ){
                    e.printStackTrace();
                }


                if (notelist.size() != 0) {
                    no_data.setVisibility(View.GONE);
                }else{
                    no_data.setVisibility(View.VISIBLE);

                }





            }
        }

        new

                GetNotesTask().

                execute();
    }


    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getnotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getnotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }


    @Override
    public void OnfileClicked (View view, Note note,int position){
        if (noteadapter.getSelectedCount()>0){
            onListItemSelect(view,note,position);
        }else {
        noteClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("viewUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);


        }

    }


    @Override
    public void OnLongClick (View view, Note note,int position){
//        onListItemSelect(view,note,position);


    }

    @SuppressLint("NewApi")
    private void onListItemSelect (View view, Note note ,int position){


        view.setForeground(getDrawable(R.drawable.ic_image_vector));

        noteadapter.toggleSelection(position);//Toggle the selection
        boolean hasCheckedItems = noteadapter.getSelectedCount() > 0;//Check if any items are already selected or not
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new androidx.appcompat.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.note_menu, menu);


                    return true;
                }

                @Override
                public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.note_menu_share:

                            mode.finish();

                            return true;
                        case R.id.note_menu_delete:
                            deletenotes(position);


                            mode.finish();

                            return true;
                        default:
                            return false;
                    }
                }

                @SuppressLint("NewApi")
                @Override
                public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                    noteadapter.removeSelection();
                    mActionMode = null;


                }
            });


        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        int totalImage = noteadapter.getItemCount();
        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(noteadapter
                    .getSelectedCount() + "/" + totalImage));
    }



    private void deletenotes(int position){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_layout, findViewById(R.id.constraint));
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        }
        view.findViewById(R.id.Yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {

//                        NotesDatabase.getDatabase(getApplicationContext()).notedao().deleteAll();


                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
//                        selected = noteadapter.getSelectedIds();//Get selected ids
//                        //Loop all selected ids
//                        for (i = (selected.size() - 1); i >= 0; i--) {
//                            if (selected.valueAt(i)) {
//                                //If current id is selected remove the item via key
//                                notelist.remove(selected.keyAt(i));
//                                noteadapter.notifyItemRemoved(selected.keyAt(i));
//                                noteadapter.notifyDataSetChanged();//notify adapter
//
//
//                            }
//                        }
//                        Toast.makeText(MainActivity.this, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast

                        selected = noteadapter.getSelectedIds();//Get selected ids
//                        //Loop all selected ids
                        for (i = (notelist.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                //If current id is selected remove the item via key
                                notelist.remove(selected.keyAt(i));
                                noteadapter.notifyItemRemoved(selected.keyAt(i));
                                noteadapter.notifyDataSetChanged();//notify adapter


                            }
                        }
                        Toast.makeText(MainActivity.this, selected.size() + " item deleted.", Toast.LENGTH_SHORT).show();//Show Toast


                    }

                }
                new DeleteNoteTask().execute();
                alertDialog.dismiss();

            }
        });
        view.findViewById(R.id.No).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    //Set action mode null after use
    public void setNullToActionMode () {
        if (mActionMode != null)
            mActionMode = null;
    }


    private void showDeleteNoteDialog (Note note){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_layout, findViewById(R.id.constraint));
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        }
        view.findViewById(R.id.Yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).notedao().deleteNote(note);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        notelist.remove(noteClickPosition);
                        noteadapter.notifyItemRemoved(noteClickPosition);
                    }

                }
                new DeleteNoteTask().execute();
                alertDialog.dismiss();

            }
        });
        view.findViewById(R.id.No).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }

    private void showad(View view) {
        MobileAds.initialize(this);
        AdLoader adLoader = new AdLoader.Builder(this, getString(R.string.native_ads))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().withMainBackgroundColor(background).build();
                        TemplateView template = view.findViewById(R.id.my_template);
                        template.setStyles(styles);
                        template.setNativeAd(nativeAd);
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());


    }

    private void loadExitDialog() {
        dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        View view = LayoutInflater.from(this).inflate(R.layout.exit_dialog ,findViewById(R.id.ks));
        view.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (minterstitialAd!=null){
                    minterstitialAd.show(MainActivity.this);
                }else {
                    finish();
                }

            }
        });
        dialog.setContentView(view);

//        view.findViewById(R.id.btn_yes).setOnClickListener {
////            if (minterstitialAd != null) {
////                minterstitialAd.show(this@MainActivity)
////            } else {
//                finish();
////            }
//        }
        showad(view);
        dialog.setContentView(view);
    }

    @Override
    public void onBackPressed() {
        dialog.show();
    }
    private void loadInterstitialAd() {
        AdRequest adRequest=new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);


                minterstitialAd=interstitialAd;
                minterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        finish();


                        Log.d("Ad test","Ad failed to show");

                    }
                    @Override
                    public void onAdShowedFullScreenContent() {


                        Log.d("Ad test","Ad showed successfully");
                    }
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        finish();

                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("gsf","fhdj");
                finish();
                minterstitialAd=null

                ;
            }

        });

    }

}