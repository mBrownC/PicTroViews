package com.example.pictroviews;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MenuApp - Actividad principal de la aplicación para captura de fotos geolocalizadas
 *
 * Esta clase implementa:
 * - Visualización de un mapa de Google para mostrar la ubicación actual
 * - Captura de fotos mediante la cámara del dispositivo
 * - Almacenamiento de imágenes con nombres basados en coordenadas GPS
 * - Seguimiento en tiempo real de la ubicación del usuario
 *
 * La aplicación utiliza Java 17 y aprovecha sus características para el manejo
 * eficiente de las coordenadas GPS y el procesamiento de imágenes.
 *
 * @author mbrown
 * @version 1.0
 */
public class MenuApp extends AppCompatActivity implements OnMapReadyCallback {

    private LocationHandlerThread locationHandlerThread;
    private Marker currentLocationMarker;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 3;
    private GoogleMap googleMap;
    private Marker photoLocationMarker;
    private LocationManager locationManager;
    private Location lastLocation;
    private TextView latitud;
    private LocationListener locationListener;
    private TextView longitud;
    private TextView hora;
    private Button returnButton;
    private ImageView photoImageView;
    private SensorManager sensorManager;
    private Sensor orientationSensor;

    // Variable para almacenar la URI de la foto que se va a tomar
    private Uri photoURI;
    // Variable para almacenar la ruta del archivo de la foto
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_app);
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d("Storage", "Almacenamiento externo disponible");
        } else {
            Log.e("Storage", "Almacenamiento externo no disponible o no escribible");
        }

        locationHandlerThread = new LocationHandlerThread("LocationThread", locationListener);
        locationHandlerThread.start();
        locationHandlerThread.prepareHandler();
        locationHandlerThread.requestLocationUpdates(locationManager);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        latitud = findViewById(R.id.latitudText);
        longitud = findViewById(R.id.longitudText);
        hora = findViewById(R.id.hourText);
        returnButton=findViewById(R.id.returnButton);

        photoImageView = findViewById(R.id.imageView);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainMenu();
            }
        });

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                showCurrentLocationOnMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        Location lastKnownLocation = getLastKnownLocation();
        if (lastKnownLocation != null) {
            showCurrentLocationOnMap(lastKnownLocation);
        }
    }

    private void showCurrentLocationOnMap(Location location) {
        if (googleMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentLocationMarker == null) {
                        currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("Ubicación Actual")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    } else {
                        currentLocationMarker.setPosition(currentLatLng);
                    }
                }
            });
        }
    }

    public void capturePhoto(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        } else {
            lastLocation = getLastKnownLocation();
            if (lastLocation != null) {
                updateUIWithNewLocation();
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Crea un archivo de imagen usando un nombre de archivo único.
     * El nombre incluye un timestamp para garantizar que sea único.
     */
    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único con timestamp
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(new Date());
        String coordinates = "";

        // Añadir coordenadas al nombre si están disponibles
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            coordinates = String.format(Locale.US, "LAT%.6f_LON%.6f", latitude, longitude);
        }

        String imageFileName = "IMG_" + timeStamp + "_" + coordinates;

        // Obtener el directorio de almacenamiento
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );

        // Guardar la ruta del archivo para usarla con intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Inicia la aplicación de cámara y configura el archivo donde se guardará la imagen.
     * Utiliza FileProvider para manejar la seguridad en versiones nuevas de Android.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Verificar que hay una aplicación para manejar la solicitud
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear el archivo donde se guardará la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error al crear el archivo
                Log.e("TakePicture", "Error al crear el archivo de imagen", ex);
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si el archivo se creó exitosamente, continuar
            if (photoFile != null) {
                // Obtener URI para el archivo usando FileProvider
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.pictroviews.fileprovider",
                        photoFile);

                // Configurar el intent para guardar la imagen en el URI especificado
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Iniciar la actividad
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Procesar la imagen capturada desde archivo (alta resolución)
                processAndDisplayImage();

                // Actualizar el marcador y la UI con la información de ubicación
                updatePhotoLocationMarker();
                updateUIWithNewLocation();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PhotoCapture", "Error al procesar la foto: " + e.getMessage());
                Toast.makeText(this, "Error al procesar la foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Procesa la imagen capturada, la muestra en la UI y la guarda en la galería.
     */
    private void processAndDisplayImage() {
        try {
            // Cargar la imagen para mostrarla en la UI
            Bitmap fullBitmap = getBitmapFromFile();

            if (fullBitmap != null) {
                // Escalar el bitmap para mostrarlo en la ImageView
                Bitmap displayBitmap = getScaledBitmap(fullBitmap, photoImageView.getWidth(), photoImageView.getHeight());
                photoImageView.setImageBitmap(displayBitmap);

                // Guardar la imagen en la galería
                saveImageToGallery(fullBitmap);
            } else {
                Log.e("ProcessImage", "No se pudo cargar la imagen desde: " + currentPhotoPath);
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ProcessImage", "Error procesando imagen: " + e.getMessage());
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Carga el bitmap desde el archivo de la foto
     */
    private Bitmap getBitmapFromFile() {
        // Obtener las dimensiones del ImageView
        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();

        // Si el ImageView no tiene dimensiones aún, usar valores predeterminados
        if (targetW <= 0) targetW = 800;
        if (targetH <= 0) targetH = 800;

        // Obtener las dimensiones del bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Calcular cuánto reducir la imagen
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decodificar el archivo de imagen en un Bitmap de tamaño reducido
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
    }

    /**
     * Escala un bitmap para ajustarse a las dimensiones especificadas
     */
    private Bitmap getScaledBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            return bitmap; // Si las dimensiones no son válidas, devolver el original
        }

        float scaleFactor = Math.min(
                (float) targetWidth / bitmap.getWidth(),
                (float) targetHeight / bitmap.getHeight());

        return Bitmap.createScaledBitmap(
                bitmap,
                Math.round(bitmap.getWidth() * scaleFactor),
                Math.round(bitmap.getHeight() * scaleFactor),
                true);
    }

    /**
     * Guarda la imagen en la galería del dispositivo
     */
    private void saveImageToGallery(Bitmap imageBitmap) {
        // Obtener coordenadas para el nombre del archivo
        String coordinates = "";
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            coordinates = String.format(Locale.US, "LAT%.6f_LON%.6f", latitude, longitude);
        }

        // Crear nombre de archivo con formato y coordenadas
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_" + coordinates;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Para Android 10 (API 29) y superior, usar el nuevo sistema de almacenamiento
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PicTroViews");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver resolver = getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    // Guardar la imagen sin comprimir para mantener la calidad
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    // Para Android 10+, marcar que ya no está pendiente
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        resolver.update(imageUri, values, null, null);
                    }

                    Toast.makeText(this, "Imagen guardada con coordenadas: " + coordinates, Toast.LENGTH_SHORT).show();
                    Log.d("SaveImage", "Imagen guardada en la galería: " + imageUri);
                    return;
                }
            }

            throw new IOException("No se pudo crear el registro en MediaStore");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveImage", "Error al guardar la imagen: " + e.getMessage());
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();

            // Si falla, eliminar el URI
            if (imageUri != null) {
                resolver.delete(imageUri, null, null);
            }
        }
    }

    private void backToMainMenu(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void updateUIWithNewLocation() {
        if (lastLocation != null && googleMap != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(Calendar.getInstance().getTime());
            String time = currentTime;
            latitud.setText("Latitud: " + latitude);
            longitud.setText("Longitud: " + longitude);
            hora.setText("Hora: " + time);
            LatLng locationLatLng = new LatLng(latitude, longitude);
            addMarker(locationLatLng);
            moveCamera(locationLatLng);
        }
    }

    private void updatePhotoLocationMarker() {
        if (lastLocation != null && googleMap != null) {
            LatLng photoLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            if (photoLocationMarker != null) {
                photoLocationMarker.remove();
            }
            photoLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(photoLatLng)
                    .title("Ubicación de la foto")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photoLatLng, 15f));
        }
    }

    private void addMarker(LatLng latLng) {
        if (photoLocationMarker != null) {
            photoLocationMarker.remove();
        }
        photoLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Ubicación de la foto")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void moveCamera(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                capturePhoto(null);
            } else {
                Toast.makeText(this, "Se necesitan todos los permisos para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            startLocationUpdates();
        }
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }

    public void startLocationUpdates() {
        if (locationHandlerThread != null) {
            locationHandlerThread.requestLocationUpdates(locationManager);
        } else {
            Log.e("PrincipalMenu", "locationHandlerThread is null");
        }
    }

    private void stopLocationUpdates() {
        locationHandlerThread.stopLocationUpdates(locationManager);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Implementación opcional
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos adicionales si es necesario
    }

    public class LocationHandlerThread extends HandlerThread {
        private Handler handler;
        private LocationListener locationListener;

        public void setLocationListener(LocationListener listener) {
            locationListener = listener;
        }

        public LocationHandlerThread(String name, LocationListener listener) {
            super(name);
            locationListener = listener;
        }

        public void postTask(Runnable task) {
            handler.post(task);
        }

        public void prepareHandler() {
            handler = new Handler(getLooper());
        }

        public void requestLocationUpdates(LocationManager locationManager) {
            if (handler == null) {
                throw new IllegalStateException("Handler not prepared. Call prepareHandler() first.");
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (locationManager != null && locationListener != null) {
                        if (ActivityCompat.checkSelfPermission(MenuApp.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MenuApp.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    }
                }
            });
        }

        public void stopLocationUpdates(LocationManager locationManager) {
            if (handler == null) {
                throw new IllegalStateException("Handler not prepared. Call prepareHandler() first.");
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (locationManager != null && locationListener != null) {
                        locationManager.removeUpdates(locationListener);
                    }
                }
            });
        }
    }
}