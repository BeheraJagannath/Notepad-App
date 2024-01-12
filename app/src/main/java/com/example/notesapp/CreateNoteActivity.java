package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notesapp.Database.NotesDatabase;
import com.example.notesapp.entities.Note;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    ImageView back ,save,select_image;
    ImageButton removeImgBtn ;
    EditText note_title ,note_subtitle ,type_start ;
    TextView datetime ;
    ImageView optionsMenu ;
    private View color_view ;
    AdView new_adview ;



    ImageView imageColor,imageColor1,imageColor2 ,imageColor3 ,imageColor4 ,imageColor5,imageColor6;

    private BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1 ;
    private static final int REQUEST_CODE_TAKE_PERMISSION = 2 ;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3 ;
    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_VOICE_NOTE = 6;

    public Note alreadyAvailable ;

    String selectedNoteColor ;
    String selectImagePath ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnew_note);

        back = findViewById(R.id.back);
        removeImgBtn = findViewById(R.id.remove_img_btn);
        note_title = findViewById(R.id.note_title);
        note_subtitle = findViewById(R.id.note_subtitle);
        type_start = findViewById(R.id.type_start);
        datetime = findViewById(R.id.date);
        save = findViewById(R.id.save);
        optionsMenu = findViewById(R.id.Milli);
        select_image = findViewById(R.id.select_image);
        color_view = findViewById(R.id.color_view);
        new_adview = findViewById(R.id.new_adView);

        AdRequest adRequest = new AdRequest.Builder().build() ;
        new_adview.loadAd(adRequest);


        selectedNoteColor = "#54000000" ;
        selectImagePath = "";



        if (getIntent().getBooleanExtra("viewUpdate", false)) {
            alreadyAvailable = (Note) getIntent().getSerializableExtra("note");
            setviewUpdateNote();
        }


        initmillisellounous();
        setSubtitleIndicatorColor();

        removeImgBtn.setOnClickListener(v -> {
            select_image.setImageBitmap(null);
            select_image.setVisibility(View.GONE);
            removeImgBtn.setVisibility(View.GONE);
            selectImagePath = "";
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savenote();
            }
        });



        datetime .setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
    }


    private void setviewUpdateNote() {
        note_title.setText(alreadyAvailable.getTitle());
        note_subtitle.setText(alreadyAvailable.getSubtitle());
        type_start .setText(alreadyAvailable.getNoteText());
        datetime.setVisibility(View.GONE);
//        datetime.setText(String.format("Edited %s", new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())));

        if (alreadyAvailable.getImagePath() != null &&  !alreadyAvailable.getImagePath().trim().isEmpty()){
            Glide.with(select_image.getContext()).load(alreadyAvailable.getImagePath()).centerCrop().into(select_image);
//            select_image.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailable.getImagePath()));
            select_image.setVisibility(View.VISIBLE);
            removeImgBtn.setVisibility(View.VISIBLE);
                selectImagePath = alreadyAvailable .getImagePath();

        }

    }


    public  void  savenote (){
        if (note_title.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Title cant be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if (note_subtitle.getText().toString().trim().isEmpty() && type_start.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Title cant be empty", Toast.LENGTH_SHORT).show();
            return;

        }
        final Note note= new Note();
        note.setTitle(note_title.getText().toString().trim());
        note.setSubtitle(note_subtitle.getText().toString().trim());
        note.setNoteText(type_start.getText().toString().trim());
        note.setDatetime(datetime.getText().toString().trim());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectImagePath);

        if (select_image.getVisibility() == View.VISIBLE) {
            note.setImagePath(selectImagePath);
        }

        if (alreadyAvailable != null){
            note.setId(alreadyAvailable.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void ,Void ,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).notedao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);

                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute() ;

    }


    private void initmillisellounous(){

        final ConstraintLayout layoutmilli = findViewById(R.id.bottom_sheet_behavior_id);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutmilli);

        optionsMenu.setOnClickListener(v -> {

            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            } else {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        imageColor = layoutmilli.findViewById(R.id.image_color);
        imageColor1 = layoutmilli.findViewById(R.id.image_color1);
        imageColor2 = layoutmilli.findViewById(R.id.image_color2);
        imageColor3 = layoutmilli.findViewById(R.id.image_color3);
        imageColor4 = layoutmilli.findViewById(R.id.image_color4);
        imageColor5 = layoutmilli.findViewById(R.id.image_color5);
        imageColor6 = layoutmilli.findViewById(R.id.image_color6);



        layoutmilli.findViewById(R.id.viewColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#54000000";
                imageColor.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#13A662";
                imageColor.setImageResource(0);
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#FF4E4E";
                imageColor.setImageResource(0);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#3B81FF";
                imageColor.setImageResource(0);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#FFFF00FF";
                imageColor. setImageResource(0);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#118E9C";
                imageColor. setImageResource(0);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                imageColor6.setImageResource(0);
                setSubtitleIndicatorColor();

            }
        });
        layoutmilli.findViewById(R.id.viewColor6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor="#FF03DAC5";
                imageColor. setImageResource(0);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();

            }
        });

        if (alreadyAvailable != null && alreadyAvailable.getColor() != null && !alreadyAvailable.getColor().trim().isEmpty()){
            switch (alreadyAvailable.getColor()){
//                case "#54000000":
//                    bottom_sheet_behavior_id.findViewById(R.id.viewColor).performClick();
//                    break;
                case "#13A662":
                    layoutmilli.findViewById(R.id.viewColor1).performClick();
                    break;
                case "#FF4E4E":
                    layoutmilli.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#3B81FF":
                    layoutmilli.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#FFFF00FF":
                    layoutmilli.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#118E9C":
                    layoutmilli.findViewById(R.id.viewColor5).performClick();
                    break;
                case "#FF03DAC5":
                    layoutmilli.findViewById(R.id.viewColor6).performClick();
                    break;


            }
        }


        layoutmilli.findViewById(R.id.load_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }else {
                    selectImage();

                }

            }
        });

        if (alreadyAvailable!=null){
            layoutmilli.findViewById(R.id.delete_note).setVisibility(View.VISIBLE);
            layoutmilli.findViewById(R.id.share).setVisibility(View.VISIBLE);

            layoutmilli.findViewById(R.id.delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    delete();
                }
            });

        }
        layoutmilli.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                String shareBody = String.valueOf(alreadyAvailable.getTitle());
                String shareSub = String.valueOf(
                        alreadyAvailable.getSubtitle() + "\n\n" +
                                alreadyAvailable.getNoteText());
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareBody);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareSub);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

            }
        });
        layoutmilli.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_TAKE_PERMISSION);
                }else {
                    takePhoto();

                }
            }
        });
        layoutmilli.findViewById(R.id.voice_recorder).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            voiceNote();
        });
    }

    @SuppressLint("ResourceType")
    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) color_view.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void voiceNote() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something to add note!");
        startActivityForResult(intent, REQUEST_CODE_VOICE_NOTE);
    }

    private void takePhoto() {
        ImagePicker.Companion.with(CreateNoteActivity.this)
                .cameraOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_TAKE_PHOTO);
    }
    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        if (intent.resolveActivity(getPackageManager())!=null){
//            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
//        }
        ImagePicker.Companion.with(CreateNoteActivity.this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
    }

    private void delete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_layout ,findViewById(R.id.constraint));
        builder.setView(view);
        AlertDialog alertDialog = builder.create() ;

        if (alertDialog.getWindow() !=null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        }
        view.findViewById(R.id.Yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                class DeleteNoteTask extends AsyncTask<Void ,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {

                        NotesDatabase.getDatabase(getApplicationContext()).notedao().deleteNote(alreadyAvailable);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted",true);
                        setResult(RESULT_OK,intent);
                        finish();
                    }

                }
                new DeleteNoteTask().execute();

            }
        });
        view.findViewById(R.id.No).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else  if (requestCode==REQUEST_CODE_TAKE_PERMISSION && grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                }
            }
            else{
                Toast.makeText(this, "Permission denied !", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data!=null){
                Uri uri = data.getData();
                if (uri !=null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        select_image.setImageBitmap(bitmap);
                        select_image.setVisibility(View.VISIBLE);
                        removeImgBtn.setVisibility(View.VISIBLE);

                        selectImagePath = getpathfromuri(uri);


                    }catch (Exception e){
                        Toast.makeText(this, e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK){
            if (data!=null){
                Uri takePhotoUri = data.getData();
                if (takePhotoUri !=null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(takePhotoUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        select_image.setImageBitmap(bitmap);
                        select_image.setVisibility(View.VISIBLE);
                        removeImgBtn.setVisibility(View.VISIBLE);

                        selectImagePath = getpathfromuri(takePhotoUri);


                    }catch (Exception e){
                        Toast.makeText(this, e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
        else if (requestCode == REQUEST_CODE_VOICE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> voiceResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                note_title.setText(voiceResult.get(0));
            }else {
                return;
            }
        }
    }
    private String getpathfromuri(Uri fromuri){
        String filepath ;
        Cursor cursor = getContentResolver().query(fromuri,null,null,null,null);
        if (cursor==null){
            filepath = fromuri.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filepath = cursor.getString(index);
            cursor.close();
        }
        return  filepath ;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}