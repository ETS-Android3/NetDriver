package lb.com.thenet.netdriver.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;

public class HomeFragment extends Fragment implements MainActivity.OnScanListener {

    private HomeViewModel homeViewModel;
    NfcAdapter nfcAdapter;
    String mBuildingCode = "";
    private TextView tvNfcContent;

    SignaturePad mSignaturePad;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Button mClearButton;
    private Button mSaveButton;


    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    Preview preview;
    CameraSelector cameraSelector;
    ImageAnalysis imageAnalysis;
    ProcessCameraProvider cameraProvider;
    Camera camera;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        verifyStoragePermissions(this.getActivity());


        final TextView textView = root.findViewById(R.id.text_home);
        tvNfcContent = root.findViewById(R.id.nfc_contents);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        homeViewModel.getLockStatus().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                textView.setText(textView.getText() + "   " + aBoolean.toString());
            }
        });

        /*
        Button btnSetTrue = root.findViewById(R.id.btnSetTrue);
        btnSetTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.setLockStatus(true);
            }
        });

         */

        //Make sure this device supports NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this.getActivity());
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this.getActivity(), "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            //TODO: make sure the user fixes the NFC error
            homeViewModel.disableNFC.setValue(true);
        }

        //Set Observer for Scan Building Service Result
        homeViewModel.getmBuildingNFC().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<ScanBuilding>>() {
            @Override
            public void onChanged(ResponseMessage<ScanBuilding> scanBuildingResponseMessage) {

                if(scanBuildingResponseMessage.success){
                    tvNfcContent.setText(scanBuildingResponseMessage.data.description);
                    tvNfcContent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    //TODO: 1. make views visible, and 2. reset items in recycler view

                }else {
                    tvNfcContent.setText("Failure: " + scanBuildingResponseMessage.errorMessage);
                    //TODO: 1. make views invisble and 2. reset items in recycler view


                }


            }
        });

        mSignaturePad = (SignaturePad) root.findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                Toast.makeText(HomeFragment.this.getActivity(), "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        mClearButton = (Button) root.findViewById(R.id.clear_button);
        mSaveButton = (Button) root.findViewById(R.id.save_button);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if (addJpgSignatureToGallery(signatureBitmap)) {
                    Toast.makeText(HomeFragment.this.getActivity(), "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeFragment.this.getActivity(), "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
                if (addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
                    Toast.makeText(HomeFragment.this.getActivity(), "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeFragment.this.getActivity(), "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            }
        });

        if(scannedBuildingActive()){
            tvNfcContent.setText(DriverRepository.scannedLocation.getValue().getLocationName());
            tvNfcContent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        }

        previewView = root.findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
        initializeCameraProvider();
        ((MainActivity)getActivity()).hideCameraScanFAB();

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initializeCameraProvider() {


        cameraProviderFuture.addListener(() -> {


            try {
                buildPreviewUseCase();
                buildImageAnalysisUseCase();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

                //cameraProvider.unbindAll();


        }, ContextCompat.getMainExecutor(this.getContext()));


    }

    private void buildPreviewUseCase() throws ExecutionException, InterruptedException {
        preview = new Preview.Builder()
                .build();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        cameraProvider = cameraProviderFuture.get();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

    }

    private void buildImageAnalysisUseCase() {
        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this.getContext()), new ImageAnalysis.Analyzer() {

            private int degreesToFirebaseRotation(int degrees) {
                switch (degrees) {
                    case 0:
                        return FirebaseVisionImageMetadata.ROTATION_0;
                    case 90:
                        return FirebaseVisionImageMetadata.ROTATION_90;
                    case 180:
                        return FirebaseVisionImageMetadata.ROTATION_180;
                    case 270:
                        return FirebaseVisionImageMetadata.ROTATION_270;
                    default:
                        throw new IllegalArgumentException(
                                "Rotation must be 0, 90, 180, or 270.");
                }
            }



            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                // insert your code here.
                if(previewView.getVisibility() == View.VISIBLE){
                    if (image == null || image.getImage() == null) {
                        return;
                    }
                    Image mediaImage = image.getImage();
                    int rotation = degreesToFirebaseRotation(rotationDegrees);
                    FirebaseVisionImage fbImage =
                            FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
                    // Pass image to an ML Kit Vision API
                    // ...
                    scanBarcode(fbImage);
                    image.close();
                }
            }
        });


    }

    private boolean scannedBuildingActive(){
        return DriverRepository.scannedLocation.getValue() != null && DriverRepository.scannedLocation.getValue().isActive(getContext());
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {


            Parcelable pe = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tagg = (Tag) pe;
            byte[] idd = tagg.getId();
            String rfid = getHex(idd);

            mBuildingCode = rfid;
            tvNfcContent.setText("Scanned Code: " + mBuildingCode);
            tvNfcContent.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        mBuildingCode = GlobalCoordinator.getInstance().getBuildingCode(mBuildingCode);

    }


    @Override
    public void onNewScan(String characters) {

    }

    @Override
    public void onNewIntent(Intent intent) {


        readFromIntent(intent);


        homeViewModel.getmDriverServices().scanBuildingNFC(mBuildingCode);
        try {
            showHideCameraPreview();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //scanBarcode();

    }

    private void showHideCameraPreview() throws ExecutionException, InterruptedException {

        if(previewView.getVisibility() == View.VISIBLE) {

            previewView.setVisibility(View.GONE);
            cameraProvider.unbind(preview);
            cameraProvider.unbind(imageAnalysis);
        }
        else if(previewView.getVisibility() == View.GONE) {
            previewView.setVisibility(View.VISIBLE);
            buildPreviewUseCase();
            buildImageAnalysisUseCase();
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);


        }

        if(!allPermissionsGranted()){
            ActivityCompat.requestPermissions(this.getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }





    private boolean allPermissionsGranted() {
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this.getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    boolean scanningImage = false;

    private void scanBarcode(FirebaseVisionImage image) {
        if (scanningImage) return;
        scanningImage = true;
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_CODE_128,
                                FirebaseVisionBarcode.FORMAT_CODE_39,
                                FirebaseVisionBarcode.FORMAT_CODE_93,
                                FirebaseVisionBarcode.FORMAT_CODABAR,
                                FirebaseVisionBarcode.FORMAT_EAN_13,
                                FirebaseVisionBarcode.FORMAT_EAN_8)
                        .build();
        //Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.barcode1);
        //FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        // ...
                        boolean found = false;
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();
                            found = true;
                            //cameraProvider.unbind(imageAnalysis);
                            //previewView.setVisibility(View.GONE);


                            Toast.makeText(HomeFragment.this.getContext(), rawValue,Toast.LENGTH_LONG).show();

                        }
                        if (found) {
                            try {
                                showHideCameraPreview();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    scanningImage = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        scanningImage = false;
                    }
                });

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomeFragment.this.getActivity(), "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        //String state = Environment.getExternalStoragePublicDirectory(albumName);



        //File root = this.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        //tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        /*
        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        File nfile = new File(dir, "myData.txt");

        try {
            FileOutputStream f = new FileOutputStream(nfile);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Hi , How are you");
            pw.println("Hello");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //Log.i(this.getClass().getName(), "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }



         */



        File file = new File(getContext().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        boolean canWrite = file.canWrite();
        if (!file.mkdirs()) {
            //Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();

    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        HomeFragment.this.getActivity().sendBroadcast(mediaScanIntent);




    }

    public boolean addSvgSignatureToGallery(String signatureSvg) {
        boolean result = false;
        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity the activity from which permissions are checked
     */

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



}