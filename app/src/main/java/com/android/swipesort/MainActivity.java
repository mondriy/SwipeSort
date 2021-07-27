package com.android.swipesort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context context, File f) {
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
    }

}

public class MainActivity extends AppCompatActivity {

    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private Button buttonAdd;
    private ConstraintLayout cl;
    private TextView tv;
    private ImageView iv;

    private List<ItemModel> items;
    private Intent intent;
    private DocumentFile srcDoc;
    private File directory;
    private String saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "SwipeSort";
    private SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.android.swipesort", MODE_PRIVATE);

        directory = new File(saveDir);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        if (prefs.getBoolean("firstRun", true)) {
            items = new ArrayList<>();
            items.add(new ItemModel(R.drawable.ic_hello));
            items.add(new ItemModel(R.drawable.ic_save));
            items.add(new ItemModel(R.drawable.ic_delete));
            items.add(new ItemModel(R.drawable.ic_fav));
            prefs.edit().putBoolean("firstRun", false).commit();
            cardTraining();
        } else {
            intentCreation();
        }
    }

    private void intentCreation() {
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/camera/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        items = new ArrayList<>();

        try {
            if (requestCode == 1) {
                if (data != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        items.add(new ItemModel(clipData.getItemAt(i).getUri()));
                    }
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Выберите больше фотографий!",
                    Toast.LENGTH_LONG).show();
            intentCreation();
        }
        cardCreating();
    }

    public void cardTraining() {
        CardStackView cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (manager.getTopPosition() == 4) {
                    intentCreation();
                }
            }

            @Override
            public void onCardRewound() {
            }

            @Override
            public void onCardCanceled() {
            }

            @Override
            public void onCardAppeared(View view, int position) {
            }

            @Override
            public void onCardDisappeared(View view, int position) {
            }
        });
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Arrays.asList(Direction.Top, Direction.Right, Direction.Left));
        manager.setCanScrollHorizontal(true);
        manager.setSwipeableMethod(SwipeableMethod.Manual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        adapter = new CardStackAdapter(items);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());
    }

    public void cardCreating() {
        final CardStackView cardStackView = findViewById(R.id.card_stack_view);
        cardStackView.setVisibility(View.VISIBLE);
        manager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }

            @Override
            public void onCardSwiped(Direction direction) {
                srcDoc = DocumentFile.fromSingleUri(getApplicationContext(), items.get(manager.getTopPosition() - 1).getImage());

                if (direction == Direction.Left) {
                    srcDoc.delete();
                }
                if (direction == Direction.Right) {

                }

                if (direction == Direction.Top) {
                    String sourceFileType = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(srcDoc.getUri()));
                    DocumentFile targetLocation = DocumentFile.fromFile(new File(saveDir + "/"));
                    DocumentFile newFile = targetLocation.createFile(sourceFileType, srcDoc.getName());
                    try {
                        copyBufferedFile(new BufferedInputStream(getContentResolver().openInputStream(srcDoc.getUri())), new BufferedOutputStream(getContentResolver().openOutputStream(newFile.getUri())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (manager.getTopPosition() == items.size()) {
                    cardStackView.setVisibility(View.GONE);

                    iv = (ImageView) findViewById(R.id.done);
                    buttonAdd = (Button) findViewById(R.id.containedButton);
                    cl = (ConstraintLayout) findViewById(R.id.layout);
                    tv = (TextView) findViewById(R.id.addText);

                    iv.setVisibility(View.VISIBLE);
                    buttonAdd.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.VISIBLE);
                    cl.setBackgroundColor(Color.WHITE);
                    buttonAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            intentCreation();
                        }
                    });
                }
                scanFile(getApplicationContext(), directory, "image/jpeg");
            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {

            }

            @Override
            public void onCardDisappeared(View view, int position) {

            }
        });
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Arrays.asList(Direction.Top, Direction.Right, Direction.Left));
        manager.setCanScrollHorizontal(true);
        manager.setSwipeableMethod(SwipeableMethod.Manual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        adapter = new CardStackAdapter(items);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());
    }

    public void scanFile(Context ctxt, File f, String mimeType) {
        MediaScannerConnection
                .scanFile(ctxt, new String[] {f.getAbsolutePath()},
                        new String[] {mimeType}, null);
    }

    void copyBufferedFile(BufferedInputStream bufferedInputStream,
                          BufferedOutputStream bufferedOutputStream)
            throws IOException {
        try (BufferedInputStream in = bufferedInputStream;
             BufferedOutputStream out = bufferedOutputStream)
        {
            byte[] buf = new byte[1024];
            int nosRead;
            while ((nosRead = in.read(buf)) != -1)
            {
                out.write(buf, 0, nosRead);
            }
        }
    }
}

