package coroutine.demo.image

import java.awt.Color

class Image(private val pixels: Array<Array<Color>>) {

    fun height(): Int {
        return pixels[0].size
    }

    fun width(): Int {
        return pixels.size
    }

    fun getPixel(x: Int, y: Int): Color {
        return pixels[x][y]
    }

}
