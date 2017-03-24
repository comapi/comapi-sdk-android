package com.comapi.helpers;

import com.comapi.internal.network.AuthManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import okhttp3.mockwebserver.MockResponse;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ResponseTestHelper {

    private static File getFileFromPath(Object obj, String fileName) {
        ClassLoader classLoader = obj.getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return new File(resource.getPath());
    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    public static String readFromFile(Object obj, String fileName) throws IOException {
        return readFile(getFileFromPath(obj, fileName));
    }

    public static MockResponse createMockResponse(Object obj, String fileName, int responseCode) throws IOException {
        String json = readFile(getFileFromPath(obj, fileName));
        MockResponse response = new MockResponse();
        response.addHeader("Authorization", AuthManager.addAuthPrefix("token123"));
        response.setResponseCode(responseCode);
        response.setBody(json);
        return response;
    }
}
