/**
 * 获取传入元素的cssSelector
 * @param dom
 * @returns {string}
 */
function getCssPath(dom) {
    var path = '';
    for (; dom && dom.nodeType == 1; dom = dom.parentNode) {
        if (dom.tagName == 'BODY') {
            break
        }
        var index = 1;
        for (var sib = dom.previousSibling; sib; sib = sib.previousSibling) {
            if (sib.nodeType == 1 && sib.tagName == dom.tagName) index++
        }
        var xname = '>' + dom.tagName.toLowerCase();
        if (dom.id) {
            xname += '#' + dom.id
        } else if (dom.className) {
            xname += '.' + dom.className.replace(new RegExp(/( )/g), '.')
        } else {
            xname += ''
        }
        path = xname + path
    }
    if (path.substring(0, 1) == '>') {
        path = path.replace('>', '')
    }
    do {
        path = path.replace('..', '.')
    } while (path.indexOf('..') > 0);
    do {
        path = path.replace('.>', '>')
    } while (path.indexOf('.>') > 0);
    var patt = /\.$/;
    //如果是以.结尾则删除
    while (patt.test(path)) {
        path = path.replace(patt, "")
    }
    //把所有的>替换为空格
    path = path.replace(new RegExp(/(>)/g), ' ');

    //去除为纯数字的class
    path = path.replace(new RegExp(/\.[\d]+/), '');
    return path;
}


/**
 * 页面预处理
 */
function pageInit() {
    /**
     * 取消页面点击事件
     */
    $("#mainiframe").contents().find("*").unbind();
    $("#mainiframe").contents().find("*").click(function (event) {
        event.preventDefault();
    });
    /**
     * 鼠标进入时自动高亮
     * @param e
     */
    $("#mainiframe").contents().find("*").mouseover(function (e) {
        var path = getCssPath(e.target);
        console.log("鼠标所在：" + path);
        $("#mainiframe").contents().find(path).css('outline', 'solid 3px #FF5E52');
    });
    /**
     * 鼠标出去时自动取消高亮
     * @param e
     */
    $("#mainiframe").contents().find("*").mouseout(function (e) {
        var path = getCssPath(e.target);
        console.log("鼠标所在：" + path);
        $("#mainiframe").contents().find(path).css('outline', '');
    });
}


/**
 * 自动调整IFrame高度
 */
function changeFrameHeight() {
    var ifm = document.getElementById("mainiframe");
    var h = $(".header").height();
    ifm.height = document.documentElement.clientHeight - h;
}