/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.relive.oauth2.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;


/**
 * 默认设备激活页面生成类。
 * 该类用于生成一个包含设备激活二维码和相关指令的HTML页面，用户可以通过扫描二维码或者手动输入激活码来完成设备激活。
 * <p>
 *
 * @author: ReLive27
 * @date: 2024/5/3 09:29
 */
class DefaultDeviceActivatePage {

    // UTF-8编码的HTML内容类型
    private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

    // 私有构造函数，防止实例化
    private DefaultDeviceActivatePage() {
    }

    /**
     * 显示设备激活页面。
     *
     * @param request                 当前的HttpServletRequest对象
     * @param response                当前的HttpServletResponse对象
     * @param userCode                设备激活码
     * @param verificationUriComplete 完整的验证URI
     * @param verificationUri         手动输入的验证URI
     * @param additionalParameters    其他附加参数，用于页面定制
     * @throws IOException 如果输入输出发生错误
     */
    static void displayDeviceActivate(HttpServletRequest request, HttpServletResponse response,
                                      String userCode, String verificationUriComplete,
                                      String verificationUri, Map<String, String> additionalParameters) throws IOException {

        // 生成设备激活页面的HTML内容
        String deviceActivatePage = generateDeviceActivatePage(request, userCode, verificationUriComplete,
                verificationUri, additionalParameters);

        // 设置响应内容类型为HTML并写入页面内容
        response.setContentType(TEXT_HTML_UTF8.toString());
        response.setContentLength(deviceActivatePage.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(deviceActivatePage);
    }

    /**
     * 生成设备激活页面的HTML内容。
     *
     * @param request                 当前的HttpServletRequest对象
     * @param userCode                设备激活码
     * @param verificationUriComplete 完整的验证URI
     * @param verificationUri         手动输入的验证URI
     * @param additionalParameters    其他附加参数，用于页面定制
     * @return 生成的HTML页面字符串
     */
    private static String generateDeviceActivatePage(HttpServletRequest request,
                                                     String userCode, String verificationUriComplete, String verificationUri,
                                                     Map<String, String> additionalParameters) {

        // 生成二维码图像
        byte[] qrCodeImage = generateQRCodeImage(verificationUriComplete);

        // 开始构建HTML页面内容
        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>");
        builder.append("<html lang=\"en\">");
        builder.append("<head>");
        builder.append("    <meta charset=\"utf-8\">");
        builder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        builder.append("    <title>iQIYI Smart TV Activation</title>");
        builder.append("    <style>");
        builder.append("        body {");
        builder.append("            font-family: Arial, sans-serif;");
        builder.append("            background-color: #1D202D;");
        builder.append("            color: #fff;");
        builder.append("            display: flex;");
        builder.append("            justify-content: center;");
        builder.append("            align-items: center;");
        builder.append("            height: 100vh;");
        builder.append("            margin: 0;");
        builder.append("        }");
        builder.append("        .content {");
        builder.append("            border: 2px solid black;");
        builder.append("            background-color: black;");
        builder.append("            text-align: center;");
        builder.append("            padding: 20px;");
        builder.append("            border-radius: 10px;");
        builder.append("            display: flex;");
        builder.append("            justify-content: center;");
        builder.append("            flex-direction: column;");
        builder.append("            width: 50%;");
        builder.append("            height: auto;");
        builder.append("        }");
        builder.append("        .container {");
        builder.append("            display: flex;");
        builder.append("            align-items: flex-start;");
        builder.append("            width: 100%;");
        builder.append("            max-width: 900px;");
        builder.append("            padding-left: 20px;");
        builder.append("            border-radius: 10px;");
        builder.append("            flex-direction: row;");
        builder.append("        }");
        builder.append("        .instructions {");
        builder.append("            text-align: left;");
        builder.append("            line-height: 1.6;");
        builder.append("            font-size: 16px;");
        builder.append("            width: 50%;");
        builder.append("            border-right: 2px solid #555;");
        builder.append("            padding-right: 20px;");
        builder.append("        }");
        builder.append("        img {");
        builder.append("            margin-top: 20px;");
        builder.append("            max-width: 100%;");
        builder.append("            height: auto;");
        builder.append("        }");
        builder.append("        .qr-code {");
        builder.append("            width: 50%;");
        builder.append("            text-align: center;");
        builder.append("        }");
        builder.append("        .activation-code {");
        builder.append("            font-size: 24px;");
        builder.append("            color: yellow;");
        builder.append("            margin-top: 20px;");
        builder.append("        }");
        builder.append("        a {");
        builder.append("            color: #09f;");
        builder.append("            text-decoration: none;");
        builder.append("        }");
        builder.append("    </style>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append("<div class=\"content\">");
        builder.append("    <h1>iQIYI Smart TV</h1>");
        builder.append("    <p>To activate this smart TV to stream directly from iQIYI</p>");
        builder.append("    <div class=\"container\">");
        builder.append("        <div class=\"instructions\">");
        builder.append("            <p>On your computer or mobile device</p>");
        builder.append("            <p><strong>1.</strong> Go to the following link:</p>");
        builder.append("            <p><a target=\"_blank\" href=\"" + verificationUriComplete + "\">" + verificationUriComplete + "</a></p>");
        builder.append("            <p><strong>2.</strong> Enter the following code:</p>");
        builder.append("            <p class=\"activation-code\">" + userCode + "</p>");
        builder.append("        </div>");
        builder.append("        <div class=\"qr-code\">");
        builder.append("            <p>OR</p>");
        builder.append("            <p>Scan the QR code below</p>");
        builder.append("            <img src=\"" + "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeImage) + "\" alt=\"QR Code\" width=\"100\">");
        builder.append("        </div>");
        builder.append("    </div>");
        builder.append("</div>");
        builder.append("</body>");
        builder.append("<script>");
        builder.append("    function sendRequestAndRedirect() {");
        builder.append("        var xhr = new XMLHttpRequest();");
        builder.append("        xhr.open(\"GET\", \"" + OAuth2DeviceAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI + "\", true);");
        builder.append("        xhr.onreadystatechange = function () {");
        builder.append("            if (xhr.readyState === XMLHttpRequest.DONE) {");
        builder.append("                if (xhr.status === 200) {");
        builder.append("                    console.log('请求成功: ' + xhr.responseText);");
        builder.append("                    var responseData = xhr.responseText;");
        builder.append("                    document.open();");
        builder.append("                    document.write(responseData);");
        builder.append("                    document.close();");
        builder.append("                } else {");
        builder.append("                    console.error('请求失败: ' + xhr.status);");
        builder.append("                }");
        builder.append("            }");
        builder.append("        };");
        builder.append("        xhr.send();");
        builder.append("    }");
        builder.append("    setInterval(sendRequestAndRedirect, 3000);");
        builder.append("</script>");
        builder.append("</html>");

        return builder.toString();
    }

    /**
     * 生成指定URI的二维码图像。
     *
     * @param qrCodeData 要编码到二维码中的URI
     * @return 生成的二维码图像字节数组
     */
    private static byte[] generateQRCodeImage(String qrCodeData) {
        try {
            // 使用ZXing库生成二维码
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // 将BufferedImage转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrCodeImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
