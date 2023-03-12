package coroutine.demo.image

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

const val BASE_PATH = "./src/main/resources/images/"

public val File.suffixName: String
    get() = name.substringAfterLast('.', "")

fun loadImage(file: File) = ImageIO.read(file).let {
    Array(it.width) { x ->
        Array(it.height) { y ->
            Color(it.getRGB(x, y))
        }
    }.let {
        Image(it)
    }
}