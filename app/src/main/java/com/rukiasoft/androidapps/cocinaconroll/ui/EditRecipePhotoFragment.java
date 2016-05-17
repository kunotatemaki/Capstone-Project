package com.rukiasoft.androidapps.cocinaconroll.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rukiasoft.androidapps.cocinaconroll.CocinaConRollApplication;
import com.rukiasoft.androidapps.cocinaconroll.R;
import com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem;
import com.rukiasoft.androidapps.cocinaconroll.database.DatabaseRelatedTools;
import com.rukiasoft.androidapps.cocinaconroll.database.RecipesTable;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.ItemTouchHelperViewHolder;
import com.rukiasoft.androidapps.cocinaconroll.dragandswipehelper.OnStartDragListener;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
import com.rukiasoft.androidapps.cocinaconroll.utilities.ReadWriteTools;
import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class EditRecipePhotoFragment extends Fragment {

    //private final static String TAG = "EditRecipePhotoFragment";
    private Uri mImageCaptureUri;
    

    private Bitmap photo;
    private RecipeItem recipeItem;
    private Tools mTools;
    private DatabaseRelatedTools dbTools;
    private ReadWriteTools rwTools;
    private String nameOfNewImage = "";

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int CROP_FROM_FILE = 4;

    @Nullable
    @BindView(R.id.create_recipe_author_edittext) EditText authorRecipe;
    @BindView(R.id.edit_recipe_photo) ImageView mImageView;
    @BindView(R.id.edit_recipe_minutes) EditText minutes;
    @BindView(R.id.edit_recipe_minutes_layout)
    TextInputLayout minutesLayout;
    @BindView(R.id.edit_recipe_portions_layout) TextInputLayout portionsLayout;
    @BindView(R.id.edit_recipe_portions) EditText portions;
    @Nullable
    @BindView(R.id.create_recipe_name_layout) TextInputLayout createRecipeNameLayout;
    @Nullable
    @BindView(R.id.create_recipe_name_edittext) EditText createRecipeName;
    @Nullable
    @BindView(R.id.edit_recipe_name)
    TextView editRecipeName;
    @BindView(R.id.checkbox_vegetarian)CheckBox checkBox;
    private Unbinder unbinder;

    public EditRecipePhotoFragment() {
        // Required empty public constructor
    }

    public String getNameOfNewImage() {
        return nameOfNewImage;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView");
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        mTools = new Tools();
        rwTools = new ReadWriteTools();
        dbTools = new DatabaseRelatedTools();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        if(getActivity().isFinishing()){
            return null;
        }
        View view;
        if(recipeItem == null){
            setRecipe();
        }
        if(((recipeItem.getState() & Constants.FLAG_OWN) != 0) &&
                ((recipeItem.getState() & Constants.FLAG_EDITED) == 0)) {
            view = inflater.inflate(R.layout.fragment_edit_recipe_foto_create, container, false);
            //author.setText(mTools.getOwnerName(getActivity()));

        }else {
            view = inflater.inflate(R.layout.fragment_edit_recipe_foto_modify, container, false);
        }
        unbinder = ButterKnife.bind(this, view);

        if(createRecipeName != null) {
            createRecipeName.addTextChangedListener(new TextWatcher() {


                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    checkIfNameExists(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        if(authorRecipe != null) {
            authorRecipe.setText(mTools.getStringFromPreferences(getActivity(), Constants.PROPERTY_DEVICE_OWNER_EMAIL));
        }
        if(editRecipeName != null){
            editRecipeName.setText(recipeItem.getName());
        }

        rwTools.loadImageFromPath(getActivity().getApplicationContext(), mImageView,
                recipeItem.getPathPicture(),
                R.drawable.default_dish, recipeItem.getVersion());

        
        if(recipeItem.getMinutes()>0)
            minutes.setText(recipeItem.getMinutes().toString());
        else
            minutes.setText("0");
        
        if(recipeItem.getPortions()>0)
            portions.setText(recipeItem.getPortions().toString());
        else
            portions.setText("0");
        
        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        android.support.v7.app.AlertDialog.Builder builder =
                                new android.support.v7.app.AlertDialog.Builder(getActivity());

                        builder.setMessage(getResources().getString(R.string.camera_explanation))
                                .setTitle(getResources().getString(R.string.permissions_title))
                                .setPositiveButton(getResources().getString(R.string.accept),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{Manifest.permission.CAMERA},
                                                        Constants.MY_PERMISSIONS_REQUEST_CAMERA);
                                            }
                                        });
                        builder.create().show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                Constants.MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                }else{
                    selectPhoto(true);
                }
            }
        });

        if(checkBox != null) {
            checkBox.setChecked(recipeItem.getVegetarian());
            checkBox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    recipeItem.setVegetarian(((CheckBox) v).isChecked());
                }
            });
        }
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion <= Build.VERSION_CODES.JELLY_BEAN){
            final float scale = this.getResources().getDisplayMetrics().density;
            checkBox.setPadding(checkBox.getPaddingLeft() + (int)(20.0f * scale + 0.5f),
                    checkBox.getPaddingTop(),
                    checkBox.getPaddingRight(),
                    checkBox.getPaddingBottom());
        }

        Spinner spinner1 = (Spinner) view.findViewById(R.id.spinner_type_dish);
        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.starters));
        list.add(getResources().getString(R.string.main_courses));
        list.add(getResources().getString(R.string.desserts));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (getActivity(), android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(dataAdapter);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        String type = "";
        if(recipeItem.getType().compareTo(Constants.TYPE_STARTERS) == 0)
            type = getResources().getString(R.string.starters);
        else if(recipeItem.getType().compareTo(Constants.TYPE_MAIN) == 0)
            type = getResources().getString(R.string.main_courses);
        else if(recipeItem.getType().compareTo(Constants.TYPE_DESSERTS) == 0)
            type = getResources().getString(R.string.desserts);
        spinner1.setSelection(dataAdapter.getPosition(type));

        mTools.hideRefreshLayout(getActivity());
        return view;
    }

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            switch (pos){
                case 0:
                    recipeItem.setType(Constants.TYPE_STARTERS);
                    break;
                case 1:
                    recipeItem.setType(Constants.TYPE_MAIN);
                    break;
                case 2:
                    recipeItem.setType(Constants.TYPE_DESSERTS);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }


    public void selectPhoto(Boolean cameraAllowed){

        final String [] items;
        if(cameraAllowed){
            items = new String [] {getResources().getString(R.string.pick_from_gallery),
                    getResources().getString(R.string.pick_from_camera)};
        }else{
            items = new String [] {getResources().getString(R.string.pick_from_gallery)};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<> (getActivity(), android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.pick_photo));
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { //pick from camera
                switch (item) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                        break;
                    case 1:
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mImageCaptureUri = Uri.fromFile(new File(rwTools.getEditedStorageDir(),
                                Constants.TEMP_CAMERA_NAME + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                        try {
                            takePictureIntent.putExtra("return-data", true);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.no_camera), Toast.LENGTH_LONG);
                            }
                            startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_camera), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doCrop(int mode) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        cropIntent.setDataAndType(mImageCaptureUri, "image/*");
        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(
                cropIntent, 0);

        if (list.size() == 0) {

            try {
                photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageCaptureUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recipeItem.setPathPicture(rwTools.saveBitmap(photo, recipeItem.getPicture()));
            //if(recipeItem.getState().compareTo(Constants.STATE_OWN) != 0)
            recipeItem.setState(Constants.FLAG_EDITED_PICTURE);

            rwTools.loadImageFromPath(getActivity().getApplicationContext(), mImageView, recipeItem.getPathPicture(),
                    R.drawable.default_dish, recipeItem.getVersion());

            return;
        }
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 4);
        cropIntent.putExtra("aspectY", 3);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 400);
        cropIntent.putExtra("outputY", 300);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, mode);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop(CROP_FROM_CAMERA);
                break;
            case PICK_FROM_FILE:
                Uri originalUri = data.getData();

                String id;
                try{
                    id = originalUri.getLastPathSegment().split(":")[1];
                }catch(Exception e){
                    id = originalUri.getLastPathSegment();
                }
                final String[] imageColumns = {MediaStore.Images.Media.DATA};

                Uri uri = getUri();
                String selectedImagePath = "path";

                Cursor cursor = getActivity().getContentResolver().query(uri, imageColumns,
                        MediaStore.Images.Media._ID + "=" + id, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    selectedImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    cursor.close();
                }

                File file = new File(selectedImagePath);
                if (file.exists()) {
                    mImageCaptureUri = Uri.fromFile(new File(selectedImagePath));
                }

                doCrop(CROP_FROM_FILE);
                break;
            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    photo = extras.getParcelable("data");
                    recipeItem.setPicture(getPictureNameFromFileName());
                    updateNameOfNewImage(recipeItem.getPicture());
                    recipeItem.setPathPicture(rwTools.saveBitmap(photo, recipeItem.getPicture()));
                    //if(recipeItem.getState().compareTo(Constants.STATE_OWN) != 0)
                    recipeItem.setState(Constants.FLAG_EDITED_PICTURE);

                    rwTools.loadImageFromPath(getActivity().getApplicationContext(),
                            mImageView, recipeItem.getPathPicture(),
                            R.drawable.default_dish, recipeItem.getVersion());
                }
                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
                break;
            case CROP_FROM_FILE:
                Bundle extras2 = data.getExtras();
                if (extras2 != null) {
                    photo = extras2.getParcelable("data");
                    recipeItem.setPicture(getPictureNameFromFileName());
                    updateNameOfNewImage(recipeItem.getPicture());
                    recipeItem.setPathPicture(rwTools.saveBitmap(photo, recipeItem.getPicture()));
                    //if(recipeItem.getState().compareTo(Constants.STATE_OWN) != 0)
                    recipeItem.setState(Constants.FLAG_EDITED_PICTURE);
                    rwTools.loadImageFromPath(getActivity().getApplicationContext(),
                            mImageView, recipeItem.getPathPicture(),
                            R.drawable.default_dish, recipeItem.getVersion());
                }
                break;
        }
    }

    private void updateNameOfNewImage(String name){
        if(!nameOfNewImage.isEmpty()){
            rwTools.deleteImageFromEditedPath(nameOfNewImage);
        }
        nameOfNewImage = name;
    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    private void setRecipe(){
        if(getActivity() instanceof EditRecipeActivity){
            recipeItem = ((EditRecipeActivity) getActivity()).getRecipe();
        }
    }

    public int getPortions(){
        try {
            return Integer.parseInt(portions.getText().toString());
        }catch(NumberFormatException e){
            return -1;
        }
    }

    public int getMinutes(){
        try {
            return Integer.parseInt(minutes.getText().toString());
        }catch(NumberFormatException e){
            return -1;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public Boolean checkInfoOk(){
        mTools.hideSoftKeyboard(getActivity());
        boolean ret = true;

        try {
            int min = Integer.valueOf(minutes.getText().toString());
            recipeItem.setMinutes(min);
        }catch (NumberFormatException e){
            minutes.setText("0");
            recipeItem.setMinutes(0);
        }

        try {
            int port = Integer.valueOf(portions.getText().toString());
            recipeItem.setPortions(port);
        }catch (NumberFormatException e){
            portions.setText("0");
            recipeItem.setPortions(0);
        }

        if(createRecipeName == null) {
            //modify case
            return ret;
        }

        //create case
        if(createRecipeNameLayout != null) {
            createRecipeNameLayout.setError(null);
        }
        portionsLayout.setError(null);
        minutesLayout.setError(null);
        String sName = createRecipeName.getText().toString();
        if(sName.isEmpty()){
            createRecipeNameLayout.setError(getResources().getString(R.string.no_recipe_name));
            ret = false;
        }

        if(authorRecipe != null) {
            recipeItem.setAuthor(authorRecipe.getText().toString());
        }

        return ret;
    }

    private void checkIfNameExists(String sName){
        List<RecipeItem> coincidences = dbTools.searchRecipesInDatabase(getActivity().getApplicationContext(),
                RecipesTable.FIELD_NAME_NORMALIZED, dbTools.getNormalizedString(sName));
        String error = null;
        if (coincidences.size() > 0) {
            error = getResources().getString(R.string.duplicated_recipe);
        }
        if(createRecipeNameLayout != null){
            createRecipeNameLayout.setError(error);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = CocinaConRollApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void onPause(){
        if(createRecipeName != null) {
            String name = createRecipeName.getText().toString();
            recipeItem.setName(name);
        }
        super.onPause();
    }

    private String getPictureNameFromFileName(){
        return mTools.getCurrentDate(getActivity()).concat(".jpg");
    }




}


