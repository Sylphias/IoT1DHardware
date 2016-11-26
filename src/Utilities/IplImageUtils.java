package Utilities;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Created by Ilya on 26/11/16.
 */
public class IplImageUtils {
        public static void main(String[] args) {
            try {

                byte[] imageInByte;
                BufferedImage originalImage = ImageIO.read(new File(
                        "c:/darksouls.jpg"));

                // convert BufferedImage to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "jpg", baos);
                baos.flush();
                imageInByte = baos.toByteArray();
                baos.close();

                // convert byte array back to BufferedImage
                InputStream in = new ByteArrayInputStream(imageInByte);
                BufferedImage bImageFromConvert = ImageIO.read(in);
                ImageIO.write(bImageFromConvert, "jpg", new File(
                        "c:/new-darksouls.jpg"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
