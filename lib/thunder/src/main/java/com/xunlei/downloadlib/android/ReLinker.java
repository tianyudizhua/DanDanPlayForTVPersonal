package com.xunlei.downloadlib.android;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReLinker {
    private static final String a = "Relinker";
    private static final String b = "lib";
    private static final int c = 5;
    private static final int d = 4096;

    static class a extends RuntimeException {
        public a(String str) {
            super(str);
        }
    }

    private ReLinker() {
    }

    private static File a(Context context) {
        return context.getDir(b, 0);
    }

    private static File a(Context context, String str) {
        return new File(a(context), System.mapLibraryName(str));
    }

    private static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void a(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] bArr = new byte[4096];
            while (true) {
                int read = inputStream.read(bArr);
                if (read != -1) {
                    outputStream.write(bArr, 0, read);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] a() {
        String str;
        StringBuilder stringBuilder;
        String[] strArr = new String[0];
        try {
            return (String[]) Build.class.getField("SUPPORTED_ABIS").get(null);
        } catch (IllegalAccessException e) {
            str = a;
            stringBuilder = new StringBuilder();
            stringBuilder.append(" getSupportedABIs IllegalAccessException, ");
            stringBuilder.append(e.getMessage());
            XLLog.e(str, stringBuilder.toString());
            e.printStackTrace();
            return strArr;
        } catch (IllegalArgumentException e2) {
            str = a;
            stringBuilder = new StringBuilder();
            stringBuilder.append(" getSupportedABIs IllegalArgumentException, ");
            stringBuilder.append(e2.getMessage());
            XLLog.e(str, stringBuilder.toString());
            e2.printStackTrace();
            return strArr;
        } catch (NoSuchFieldException e3) {
            str = a;
            stringBuilder = new StringBuilder();
            stringBuilder.append(" getSupportedABIs NoSuchFieldException, ");
            stringBuilder.append(e3.getMessage());
            XLLog.e(str, stringBuilder.toString());
            e3.printStackTrace();
            return strArr;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x01ad A:{SYNTHETIC, Splitter: B:106:0x01ad} */
    /* JADX WARNING: Missing block: B:33:0x0064, code:
            if (r1.length <= 0) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:59:?, code:
            a(r7);
            a(r8);
            r1.setReadable(true, false);
            r1.setExecutable(true, false);
            r1.setWritable(true);
     */
    @android.annotation.SuppressLint({"NewApi"})
    private static void b(android.content.Context r14, java.lang.String r15) {
        /*
        r0 = "Relinker";
        r1 = "unpackLibrary";
        com.xunlei.downloadlib.android.XLLog.i(r0, r1);
        r0 = 0;
        r1 = r14.getApplicationInfo();	 Catch:{ Exception -> 0x018a }
        r2 = 0;
        r3 = 0;
    L_0x000e:
        r4 = r3 + 1;
        r5 = 5;
        r6 = 1;
        if (r3 >= r5) goto L_0x0027;
    L_0x0014:
        r3 = new java.util.zip.ZipFile;	 Catch:{ IOException -> 0x0021 }
        r7 = new java.io.File;	 Catch:{ IOException -> 0x0021 }
        r8 = r1.sourceDir;	 Catch:{ IOException -> 0x0021 }
        r7.<init>(r8);	 Catch:{ IOException -> 0x0021 }
        r3.<init>(r7, r6);	 Catch:{ IOException -> 0x0021 }
        goto L_0x0028;
    L_0x0021:
        r3 = move-exception;
        r3.printStackTrace();	 Catch:{ Exception -> 0x018a }
        r3 = r4;
        goto L_0x000e;
    L_0x0027:
        r3 = r0;
    L_0x0028:
        if (r3 != 0) goto L_0x0054;
    L_0x002a:
        r14 = "Relinker";
        r15 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r15.<init>();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r0 = "zipFile == null, path=";
        r15.append(r0);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r0 = r1.sourceDir;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r15.append(r0);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r15 = r15.toString();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        com.xunlei.downloadlib.android.XLLog.i(r14, r15);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        if (r3 == 0) goto L_0x004c;
    L_0x0044:
        r3.close();	 Catch:{ IOException -> 0x0048 }
        return;
    L_0x0048:
        r14 = move-exception;
        r14.printStackTrace();
    L_0x004c:
        return;
    L_0x004d:
        r14 = move-exception;
        goto L_0x01ab;
    L_0x0050:
        r14 = move-exception;
        r0 = r3;
        goto L_0x018b;
    L_0x0054:
        r1 = 0;
    L_0x0055:
        r4 = r1 + 1;
        if (r1 >= r5) goto L_0x017c;
    L_0x0059:
        r1 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r7 = 21;
        if (r1 < r7) goto L_0x0066;
    L_0x005f:
        r1 = a();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r7 = r1.length;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        if (r7 > 0) goto L_0x0071;
    L_0x0066:
        r1 = 2;
        r1 = new java.lang.String[r1];	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r7 = android.os.Build.CPU_ABI;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1[r2] = r7;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r7 = android.os.Build.CPU_ABI2;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1[r6] = r7;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x0071:
        r7 = r1.length;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r9 = r0;
        r10 = r9;
        r8 = 0;
    L_0x0075:
        if (r8 >= r7) goto L_0x00b6;
    L_0x0077:
        r9 = r1[r8];	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r10.<init>();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r11 = "lib/";
        r10.append(r11);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r10.append(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r9 = "/";
        r10.append(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r9 = java.lang.System.mapLibraryName(r15);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r10.append(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r9 = r10.toString();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r10 = r3.getEntry(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r11 = "Relinker";
        r12 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r12.<init>();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r13 = "zipFile.getEntry, jniNameInApk=";
        r12.append(r13);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r12.append(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r12 = r12.toString();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        com.xunlei.downloadlib.android.XLLog.i(r11, r12);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        if (r10 == 0) goto L_0x00b3;
    L_0x00b2:
        goto L_0x00b6;
    L_0x00b3:
        r8 = r8 + 1;
        goto L_0x0075;
    L_0x00b6:
        if (r10 != 0) goto L_0x00cd;
    L_0x00b8:
        r14 = "Relinker";
        r0 = "Does not exist in the APK";
        com.xunlei.downloadlib.android.XLLog.e(r14, r0);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        if (r9 == 0) goto L_0x00c7;
    L_0x00c1:
        r14 = new com.xunlei.downloadlib.android.ReLinker$a;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r14.<init>(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        throw r14;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x00c7:
        r14 = new com.xunlei.downloadlib.android.ReLinker$a;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r14.<init>(r15);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        throw r14;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x00cd:
        r1 = a(r14, r15);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1.delete();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r7 = r1.createNewFile();	 Catch:{ IOException -> 0x015e }
        if (r7 != 0) goto L_0x00e3;
    L_0x00da:
        r1 = "Relinker";
        r7 = "outputFile.createNewFile() failed";
        com.xunlei.downloadlib.android.XLLog.i(r1, r7);	 Catch:{ IOException -> 0x015e }
        goto L_0x0179;
    L_0x00e3:
        r7 = r3.getInputStream(r10);	 Catch:{ FileNotFoundException -> 0x0135, IOException -> 0x0111, all -> 0x010d }
        r8 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x010a, IOException -> 0x0107, all -> 0x0104 }
        r8.<init>(r1);	 Catch:{ FileNotFoundException -> 0x010a, IOException -> 0x0107, all -> 0x0104 }
        a(r7, r8);	 Catch:{ FileNotFoundException -> 0x0102, IOException -> 0x0100 }
        a(r7);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        a(r8);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1.setReadable(r6, r2);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1.setExecutable(r6, r2);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1.setWritable(r6);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        goto L_0x017c;
    L_0x0100:
        r1 = move-exception;
        goto L_0x0114;
    L_0x0102:
        r1 = move-exception;
        goto L_0x0138;
    L_0x0104:
        r14 = move-exception;
        r8 = r0;
        goto L_0x0157;
    L_0x0107:
        r1 = move-exception;
        r8 = r0;
        goto L_0x0114;
    L_0x010a:
        r1 = move-exception;
        r8 = r0;
        goto L_0x0138;
    L_0x010d:
        r14 = move-exception;
        r7 = r0;
        r8 = r7;
        goto L_0x0157;
    L_0x0111:
        r1 = move-exception;
        r7 = r0;
        r8 = r7;
    L_0x0114:
        r9 = "Relinker";
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0156 }
        r10.<init>();	 Catch:{ all -> 0x0156 }
        r11 = " copy(inputStream, fileOut), IOException, ";
        r10.append(r11);	 Catch:{ all -> 0x0156 }
        r1 = r1.getMessage();	 Catch:{ all -> 0x0156 }
        r10.append(r1);	 Catch:{ all -> 0x0156 }
        r1 = r10.toString();	 Catch:{ all -> 0x0156 }
        com.xunlei.downloadlib.android.XLLog.e(r9, r1);	 Catch:{ all -> 0x0156 }
        a(r7);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x0131:
        a(r8);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        goto L_0x0179;
    L_0x0135:
        r1 = move-exception;
        r7 = r0;
        r8 = r7;
    L_0x0138:
        r9 = "Relinker";
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0156 }
        r10.<init>();	 Catch:{ all -> 0x0156 }
        r11 = " copy(inputStream, fileOut), FileNotFoundException, ";
        r10.append(r11);	 Catch:{ all -> 0x0156 }
        r1 = r1.getMessage();	 Catch:{ all -> 0x0156 }
        r10.append(r1);	 Catch:{ all -> 0x0156 }
        r1 = r10.toString();	 Catch:{ all -> 0x0156 }
        com.xunlei.downloadlib.android.XLLog.e(r9, r1);	 Catch:{ all -> 0x0156 }
        a(r7);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        goto L_0x0131;
    L_0x0156:
        r14 = move-exception;
    L_0x0157:
        a(r7);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        a(r8);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        throw r14;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x015e:
        r1 = move-exception;
        r7 = "Relinker";
        r8 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r8.<init>();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r9 = "IOException ignored, ";
        r8.append(r9);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1 = r1.getMessage();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r8.append(r1);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        r1 = r8.toString();	 Catch:{ Exception -> 0x0050, all -> 0x004d }
        com.xunlei.downloadlib.android.XLLog.i(r7, r1);	 Catch:{ Exception -> 0x0050, all -> 0x004d }
    L_0x0179:
        r1 = r4;
        goto L_0x0055;
    L_0x017c:
        if (r3 == 0) goto L_0x01aa;
    L_0x017e:
        r3.close();	 Catch:{ IOException -> 0x0182 }
        return;
    L_0x0182:
        r14 = move-exception;
        r14.printStackTrace();
        return;
    L_0x0187:
        r14 = move-exception;
        r3 = r0;
        goto L_0x01ab;
    L_0x018a:
        r14 = move-exception;
    L_0x018b:
        r15 = "Relinker";
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0187 }
        r1.<init>();	 Catch:{ all -> 0x0187 }
        r2 = "unpackLibrary, Exception=";
        r1.append(r2);	 Catch:{ all -> 0x0187 }
        r14 = r14.getMessage();	 Catch:{ all -> 0x0187 }
        r1.append(r14);	 Catch:{ all -> 0x0187 }
        r14 = r1.toString();	 Catch:{ all -> 0x0187 }
        com.xunlei.downloadlib.android.XLLog.e(r15, r14);	 Catch:{ all -> 0x0187 }
        if (r0 == 0) goto L_0x01aa;
    L_0x01a7:
        r0.close();	 Catch:{ IOException -> 0x0182 }
    L_0x01aa:
        return;
    L_0x01ab:
        if (r3 == 0) goto L_0x01b5;
    L_0x01ad:
        r3.close();	 Catch:{ IOException -> 0x01b1 }
        goto L_0x01b5;
    L_0x01b1:
        r15 = move-exception;
        r15.printStackTrace();
    L_0x01b5:
        throw r14;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xunlei.downloadlib.android.ReLinker.b(android.content.Context, java.lang.String):void");
    }

    public static void loadLibrary(Context context, String str) {
        if (context == null) {
            throw new IllegalArgumentException("Given context is null");
        } else if (TextUtils.isEmpty(str)) {
            throw new IllegalArgumentException("Given library is either null or empty");
        } else {
            String str2 = null;
            File file;
            StringBuilder stringBuilder;
            try {
                try {
                    str2 = (String) ApplicationInfo.class.getField("nativeLibraryDir").get(context.getApplicationInfo());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                } catch (NoSuchFieldException e3) {
                    e3.printStackTrace();
                }
                if (str2 == null) {
                    System.loadLibrary(str);
                    return;
                }
                file = new File(str2, System.mapLibraryName(str));
                if (file.exists()) {
                    System.load(file.getAbsolutePath());
                    return;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("so file not exist, path=");
                stringBuilder.append(file.getAbsolutePath());
                throw new UnsatisfiedLinkError(stringBuilder.toString());
            } catch (UnsatisfiedLinkError e4) {
                String str3 = a;
                stringBuilder = new StringBuilder();
                stringBuilder.append("loadLibrary, linkError=");
                stringBuilder.append(e4.getMessage());
                XLLog.e(str3, stringBuilder.toString());
                file = a(context, str);
                String str4 = a;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("loadLibrary, workaroundFile=");
                stringBuilder2.append(file.getPath());
                XLLog.i(str4, stringBuilder2.toString());
                if (!file.exists()) {
                    synchronized (ReLinker.class) {
                        if (!file.exists()) {
                            b(context, str);
                        }
                    }
                }
                System.load(file.getAbsolutePath());
            } catch (Throwable th) {
                str = a;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("loadLibrary exception=");
                stringBuilder3.append(th.getMessage());
                XLLog.e(str, stringBuilder3.toString());
            }
        }
    }
}
