package com.example.market;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";

    private static final String CLOUD_NAME = "db489jsgx";
    private static final String UPLOAD_PRESET = "market_preset";
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    callback.onError("Cannot open image");
                    return;
                }

                // Читаем байты из файла
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                byte[] imageBytes = baos.toByteArray();

                // Создаем multipart запрос
                String boundary = "---CloudinaryUpload" + System.currentTimeMillis() + "---";
                HttpURLConnection connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();

                // upload_preset
                writeField(outputStream, boundary, "upload_preset", UPLOAD_PRESET);

                // file
                outputStream.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
                outputStream.write("Content-Disposition: form-data; name=\"file\"; filename=\"car.jpg\"\r\n".getBytes("UTF-8"));
                outputStream.write("Content-Type: image/jpeg\r\n\r\n".getBytes("UTF-8"));
                outputStream.write(imageBytes);
                outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // Читаем ответ
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream responseStream = connection.getInputStream();
                    String response = new String(responseStream.readAllBytes(), "UTF-8");
                    responseStream.close();

                    Log.d(TAG, "Response: " + response);

                    // Парсим secure_url из JSON
                    String secureUrl = extractJsonValue(response, "secure_url");
                    if (secureUrl != null && !secureUrl.isEmpty() && secureUrl.startsWith("https")) {
                        Log.d(TAG, "Uploaded URL: " + secureUrl);
                        callback.onSuccess(secureUrl);
                    } else {
                        callback.onError("No URL in response: " + response);
                    }
                } else {
                    callback.onError("Upload failed with code: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static void writeField(OutputStream os, String boundary, String name, String value) throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes("UTF-8"));
        os.write(value.getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));
    }

    private static String extractJsonValue(String json, String key) {
        // secure_url
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            searchKey = "\"" + key + "\": \"";
            start = json.indexOf(searchKey);
        }
        if (start == -1) return null;

        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;

        String value = json.substring(start, end);
        // Убираем экранирование слешей
        value = value.replace("\\/", "/");

        return value;
    }

    // Загрузка нескольких фото
    public static void uploadMultipleImages(Context context, java.util.List<Uri> uris, MultipleUploadCallback callback) {
        java.util.List<String> urls = new java.util.ArrayList<>();
        uploadNext(context, uris, 0, urls, callback);
    }

    private static void uploadNext(Context context, java.util.List<Uri> uris, int index,
                                   java.util.List<String> urls, MultipleUploadCallback callback) {
        if (index >= uris.size()) {
            callback.onComplete(urls);
            return;
        }

        uploadImage(context, uris.get(index), new UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                urls.add(imageUrl);
                uploadNext(context, uris, index + 1, urls, callback);
            }
            @Override
            public void onError(String error) {
                urls.add("placeholder");
                uploadNext(context, uris, index + 1, urls, callback);
            }
        });
    }

    public interface MultipleUploadCallback {
        void onComplete(java.util.List<String> imageUrls);
    }
}