package coroutine.demo.http

data class RepoList(
    var count: Int?,
    var items: List<Repo>?,
    var msg: String?
)

data class Repo(
    var added_stars: String?,
    var avatars: List<String>?,
    var desc: String?,
    var forks: String?,
    var lang: String?,
    var repo: String?,
    var repo_link: String?,
    var stars: String?
)

data class HttpResult(
    var errorCode: Int,
    var errorMsg: String?,
    var data: Data
)

data class Data(
    var offset: Int,
    var size: Int,
    var total: Int,
    var pageCount: Int,
    var curPage: Int,
    var over: Boolean,
    var datas: List<Datas>?
)

data class Datas(
    var id: Int,
    var originId: Int,
    var title: String,
    var chapterId: Int,
    var chapterName: String?,
    var envelopePic: Any,
    var link: String,
    var author: String,
    var origin: Any,
    var publishTime: Long,
    var zan: Any,
    var desc: Any,
    var visible: Int,
    var niceDate: String,
    var courseId: Int,
    var collect: Boolean
)