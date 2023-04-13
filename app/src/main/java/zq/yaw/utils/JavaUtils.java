package zq.yaw.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.CookieManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;

public class JavaUtils {

    public static String getDateFromNanoTime(Long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(time);
    }

    public static String getFileName(String url) {
        int start = url.lastIndexOf("/");
        int end = url.length();
        if (start != -1) {
            return url.substring(start + 1, end);
        } else {
            return null;
        }
    }

    public static void deleteCookiesForDomain(String domain) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null) return;


        String cookieGlob = cookieManager.getCookie(domain);
        if (cookieGlob != null) {
            String[] cookies = cookieGlob.split(";");
            for (String cookieTuple : cookies) {
                String[] cookieParts = cookieTuple.split("=");
                HashSet<String> domainSet = getDomainSet(domain);
                for (String dm : domainSet) {
                    /* Set an expire time so that this field will be removed after calling sync() */
                    cookieManager.setCookie(dm, cookieParts[0] + "=; Expires=Wed, 31 Dec 1970 23:59:59 GMT");
                }
            }
            cookieManager.flush();
        }
    }

    private static HashSet<String> getDomainSet(String domain) {
        HashSet<String> domainSet = new HashSet<>();
        String host = Uri.parse(domain).getHost();

        domainSet.add(host);
        domainSet.add("." + host);
        // exclude domain like "baidu.com"
        if (host.indexOf(".") != host.lastIndexOf(".")) {
            domainSet.add(host.substring(host.indexOf('.')));
        }

        return domainSet;
    }

    public static Bitmap createQrCode(String str) throws WriterException {
        //生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 600, 600);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        //二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }

            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;

    }
}
