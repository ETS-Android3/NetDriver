package lb.com.thenet.netdriver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.material.chip.Chip;

import java.io.IOException;
import java.util.ArrayList;

import lb.com.thenet.netdriver.camerabarcode.barcodedetection.BarcodeField;
import lb.com.thenet.netdriver.camerabarcode.barcodedetection.BarcodeProcessor;
import lb.com.thenet.netdriver.camerabarcode.barcodedetection.BarcodeResultFragment;
import lb.com.thenet.netdriver.camerabarcode.camera.CameraSource;
import lb.com.thenet.netdriver.camerabarcode.camera.CameraSourcePreview;
import lb.com.thenet.netdriver.camerabarcode.camera.GraphicOverlay;
import lb.com.thenet.netdriver.camerabarcode.camera.WorkflowModel;
import lb.com.thenet.netdriver.camerabarcode.settings.SettingsActivity;

public class LiveBarcodeScanningActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = "LiveBarcodeActivity";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private View settingsButton;
    private View flashButton;
    private Chip promptChip;
    private AnimatorSet promptChipAnimator;
    private WorkflowModel workflowModel;
    private WorkflowModel.WorkflowState currentWorkflowState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_barcode);
        preview = findViewById(R.id.camera_preview);
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay);
        graphicOverlay.setOnClickListener(this);
        cameraSource = new CameraSource(graphicOverlay);

        promptChip = findViewById(R.id.bottom_prompt_chip);
        promptChipAnimator =
                (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter);
        promptChipAnimator.setTarget(promptChip);

        findViewById(R.id.close_button).setOnClickListener(this);
        flashButton = findViewById(R.id.flash_button);
        flashButton.setOnClickListener(this);
        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);

        setUpWorkflowModel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        workflowModel.markCameraFrozen();
        settingsButton.setEnabled(true);
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
        cameraSource.setFrameProcessor(new BarcodeProcessor(graphicOverlay, workflowModel));
        workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        BarcodeResultFragment.dismiss(getSupportFragmentManager());
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
        stopCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.close_button) {
            onBackPressed();

        } else if (id == R.id.flash_button) {
            if (flashButton.isSelected()) {
                flashButton.setSelected(false);
                cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                flashButton.setSelected(true);
                cameraSource.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }

        } else if (id == R.id.settings_button) {
            // Sets as disabled to prevent the user from clicking on it too fast.
            settingsButton.setEnabled(false);
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    private void startCameraPreview() {
        if (!workflowModel.isCameraLive() && cameraSource != null) {
            try {
                workflowModel.markCameraLive();
                preview.start(cameraSource);
            } catch (IOException e) {
                //Log.e(TAG, "Failed to start camera preview!", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void stopCameraPreview() {
        if (workflowModel.isCameraLive()) {
            workflowModel.markCameraFrozen();
            flashButton.setSelected(false);
            preview.stop();
        }
    }

    private void setUpWorkflowModel() {
        workflowModel = ViewModelProviders.of(this).get(WorkflowModel.class);

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.workflowState.observe(
                this,
                workflowState -> {
                    if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                        return;
                    }

                    currentWorkflowState = workflowState;
                    //Log.d(TAG, "Current workflow state: " + currentWorkflowState.name());

                    boolean wasPromptChipGone = (promptChip.getVisibility() == View.GONE);

                    switch (workflowState) {
                        case DETECTING:
                            promptChip.setVisibility(View.VISIBLE);
                            promptChip.setText(R.string.prompt_point_at_a_barcode);
                            startCameraPreview();
                            break;
                        case CONFIRMING:
                            promptChip.setVisibility(View.VISIBLE);
                            promptChip.setText(R.string.prompt_move_camera_closer);
                            startCameraPreview();
                            break;
                        case SEARCHING:
                            promptChip.setVisibility(View.VISIBLE);
                            promptChip.setText(R.string.prompt_searching);
                            stopCameraPreview();
                            break;
                        case DETECTED:
                        case SEARCHED:
                            promptChip.setVisibility(View.GONE);
                            stopCameraPreview();
                            break;
                        default:
                            promptChip.setVisibility(View.GONE);
                            break;
                    }

                    boolean shouldPlayPromptChipEnteringAnimation =
                            wasPromptChipGone && (promptChip.getVisibility() == View.VISIBLE);
                    if (shouldPlayPromptChipEnteringAnimation && !promptChipAnimator.isRunning()) {
                        promptChipAnimator.start();
                    }
                });

        workflowModel.detectedBarcode.observe(
                this,
                barcode -> {
                    if (barcode != null) {
                        ArrayList<BarcodeField> barcodeFieldList = new ArrayList<>();
                        barcodeFieldList.add(new BarcodeField("Raw Value", barcode.getRawValue()));
                        BarcodeResultFragment.show(getSupportFragmentManager(), barcodeFieldList, new BarcodeResultFragment.onBarcodeClickListener() {
                            @Override
                            public void bacodeClicked(String rawValue) {

                                Intent intent=new Intent();
                                intent.putExtra("BARCODE",rawValue);
                                setResult(MainActivity.BARCODE_ACTIVITY_REQUEST_CODE,intent);
                                finish();//finishing activity
                                //onBackPressed();
                            }
                        });
                    }
                });
    }
}
