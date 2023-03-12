package coroutine.demo.image

import java.io.File

fun main() {
    val image = loadImage(File("${BASE_PATH}image.png"))
    println("image height: ${image.height()}, width: ${image.width()}")
    image.flipHorizontal().write(File("${BASE_PATH}image_h.png"))
    image.flipVertical().write(File("${BASE_PATH}image_v.png"))
    image.crop(0, 0, 200, 200).write(File("${BASE_PATH}image_c.png"))
}
