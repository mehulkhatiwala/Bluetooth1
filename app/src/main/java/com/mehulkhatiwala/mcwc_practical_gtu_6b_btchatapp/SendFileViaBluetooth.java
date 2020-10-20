package com.mehulkhatiwala.mcwc_practical_gtu_6b_btchatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendFileViaBluetooth extends AppCompatActivity {

    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    private static final String TAG = "SendFileViaBluetooth";

    Button openDialogBtn, sendFileBtn;
    TextView tvFolder;
    ListView listView;
    EditText filePath;

    private AlertDialog dialog;

    private File root, curFolder;
    private List<String> fileList = new ArrayList<String>();
    private List<String> fileListForDisplay = new ArrayList<String>();
    private List<Integer> imageListForDisplay = new ArrayList<Integer>();

    private final String filePathStatic = "/storage/emulated/0/Android/data/com.mehulkhatiwala.mcwc_practical_gtu_6b_btchatapp/";


    private static final String[] INITIAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int INITIAL_REQUEST = 1337;

    private static final int REQUEST_WRITE_STORAGE = INITIAL_REQUEST + 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file_via_bluetooth);

        openDialogBtn = findViewById(R.id.btnSelectFile);
        sendFileBtn = findViewById(R.id.btnSendFile);

        filePath = findViewById(R.id.filepath);
        filePath.setEnabled(false);

        if (!canAccessLocation() || !canAccessCamera() || !canAccessWriteStorage() || !canAccessReadStorage() || !canAccessReadContacts() || !canAccessWriteContacts()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }

        openDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialog(R.style.DialogAnimation);
            }
        });

        sendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendViaBluetooth();
            }
        });

        //root = new File(this.getExternalFilesDir(null).getAbsolutePath());
        root = new File(filePathStatic);
        curFolder = root;
        Log.d(TAG, "file path: " + root);
    }
    private boolean canAccessWriteStorage() {
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean canAccessReadStorage() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean canAccessReadContacts() {
        return (hasPermission(Manifest.permission.READ_CONTACTS));
    }

    private boolean canAccessWriteContacts() {
        return (hasPermission(Manifest.permission.WRITE_CONTACTS));
    }

    private boolean canAccessCamera() {
        return (hasPermission(Manifest.permission.CAMERA));
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    private void buildDialog(int animationSource) {
        fileListForDisplay.clear();
        fileList.clear();

        curFolder = new File(filePathStatic);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SendFileViaBluetooth.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        tvFolder = mView.findViewById(R.id.folder);
        listView = mView.findViewById(R.id.dialoglist);

        listDir(curFolder);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selected = new File(fileList.get(position));
                if (selected.isDirectory()) {
                    listDir(selected);
                } else if (selected.isFile()) {
                    getSelectedFile(selected);
                }
            }
        });
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        dialog.setTitle("File Selector");
        dialog.getWindow().getAttributes().windowAnimations = animationSource;
        dialog.show();
    }


    private void listDir(File f) {
        curFolder = f;
        String currentFolder = curFolder.toString();
        String filename = null;
        currentFolder = currentFolder.substring(currentFolder.lastIndexOf("/") + 1);
        tvFolder.setText(currentFolder);
        filePath.setText(f.getAbsolutePath());
        File[] files = f.listFiles();

        fileList.clear();
        fileListForDisplay.clear();

        if (files.length > 0) {
            for (File file : files) {
                filename = file.toString();
                filename = filename.substring(filename.lastIndexOf("/") + 1);//path.substring(path.lastIndexOf("/")+1);
                fileList.add(file.getPath());
                //file.isDirectory()
                fileListForDisplay.add(filename);
//                if(file.isDirectory()){
//                    imageListForDisplay.add(R.drawable.ic_baseline_folder_24);
//                }else{
//                    imageListForDisplay.add(R.drawable.ic_baseline_insert_drive_file_24);
//                }
            }
        } else {
            Toast.makeText(this, "Required folder doesn't exist.", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileListForDisplay);
        listView.setAdapter(directoryList);
        //CustomAdapter customAdapter = new CustomAdapter(SendFileViaBluetooth.this,fileListForDisplay,imageListForDisplay);
        //listView.setAdapter(customAdapter);
    }


    public void getSelectedFile(File f) {
        filePath.setText(f.getAbsolutePath());
        fileList.clear();
        dialog.hide();
    }


    //Method for send file via bluetooth
    public void sendViaBluetooth() {
        if (TextUtils.isEmpty(filePath.getText().toString())) {
            Toast.makeText(this, "Please select file first", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
        } else {
            enableBluetooth();
        }
    }

    public void enableBluetooth() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }

    //Override method for sending data via bluetooth availability--------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLU) {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("*/*");

            File file = new File(filePath.getText().toString());

            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

            if (appsList.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for (ResolveInfo info : appsList) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Bluetooth havn't been found",
                            Toast.LENGTH_LONG).show();
                } else {
                    intent.setClassName(packageName, className);
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, "Bluetooth is cancelled", Toast.LENGTH_LONG).show();
        }
    }




    public class CustomAdapter extends ArrayAdapter<String> {
        private Activity mContext;
        private List<String> fileNames;
        private List<Integer> imageId;

        public CustomAdapter(Activity context, List<String> fileNames, List<Integer> imageId) {
            super(context, R.layout.custom_listview);
            this.mContext = context;
            this.fileNames = fileNames;
            this.imageId = imageId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "in getView() method.");
            LayoutInflater inflater = mContext.getLayoutInflater();
            View row = inflater.inflate(R.layout.custom_listview, null, true);

            TextView textView = row.findViewById(R.id.textView);
            ImageView imageFlag = row.findViewById(R.id.imageView);

            textView.setText(fileNames.get(position));
            //imageFlag.setImageResource(imageId.get(position));
            return row;

        }
    }
}

