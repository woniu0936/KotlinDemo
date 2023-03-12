package coroutine.demo.image

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun Image.flipHorizontal(): Image {
    val height = height()
    val width = width()
    val pixels = Array(width) { x ->
        Array(height) { y ->
            getPixel(width - 1 - x, y)
        }
    }
    return Image(pixels)
}

fun Image.flipVertical(): Image {
    val height = height()
    val width = width()
    val pixels = Array(width) { x ->
        Array(height) { y ->
            getPixel(x, height - 1 - y)
        }
    }
    return Image(pixels)
}

fun Image.crop(startX: Int, startY: Int, width: Int, height: Int): Image {
    val pixels = Array(width) { x ->
        Array(height) { y ->
            getPixel(startX + x, startY + y)
        }
    }
    return Image(pixels)
}

fun Image.write(file: File): Boolean {
    val width = width()
    val height = height()
    val bufferImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    Array(height) { y ->
        Array(width) { x ->
            val color = getPixel(x, y)
            bufferImage.setRGB(x, y, color.rgb)
        }
    }
    return ImageIO.write(bufferImage, file.suffixName, file)
}