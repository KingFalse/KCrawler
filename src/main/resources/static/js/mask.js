$.fn.extend({
    /**
     * 给元素添加遮罩层
     * @param  message  {String}  [可选]遮罩层显示内容
     */
    mask: function (message, callback, timeout, isclickclose) {
        if (!isclickclose) {
            $(".rhui-mask").unbind();
        }
        var $target = this,
            fixed = false,
            targetStatic = true;

        if (Object.prototype.toString.call(message) !== '[object String]' || !message) {
            //如果message为空或者不是字符串，则用默认的消息提示。
            message = '请稍候。。。';
        }

        if ($target.length === 0) {
            $target = $('body');
        } else {
            if ($target.length > 1) {
                $target = $target.first();
            }

            if ($target[0] === window || $target[0] === document) {
                $target = $('body');
            }
        }

        if ($target[0] === document.body) {
            fixed = true;
        }

        //如果目标元素已经有遮罩层，获取遮罩层
        var old = $target.data('rhui.mask');
        if (old) {
            old.$content.html(message);
            center($target, old.$content, fixed);

            if (!isNaN(timeout)) {
                var p = setTimeout(function () {
                    $target.unmask(callback);
                }, timeout);
            }
            return;
        }

        //如果被遮盖的元素是static，把元素改成relative
        if ($target.css('position') === 'static') {
            targetStatic = true;
            $target.css('position', 'relative');
        }

        var $content, $overlay;
        if (fixed) {
            $overlay = $('<div class="rhui-mask" style="position:fixed;"></div>');
            $content = $('<div class="rhui-mask-content" style="position:fixed;">' + message + '</div>');
        } else {
            $overlay = $('<div class="rhui-mask"></div>');
            $content = $('<div class="rhui-mask-content">' + message + '</div>');
        }

        $overlay.appendTo($target);
        $content.appendTo($target);

        //显示遮罩层
        $overlay.show();
        $content.show();

        //让遮罩层居中
        center($target, $content, fixed);

        //把遮罩层信息添加到$target
        $target.data('rhui.mask', {
            fixed: fixed,
            $overlay: $overlay,
            $content: $content,
            targetStatic: targetStatic
        });

        /**
         * 让遮罩层内容居中显示
         * @param  $target   被遮盖的元素
         * @param  $content  遮罩层内容元素
         * @param  fixed     遮罩层是否固定显示
         */
        function center($target, $content, fixed) {
            var $window,
                height = $content.outerHeight(true),
                width = $content.outerWidth(true);

            if (fixed) {
                //如果遮罩层固定显示，让遮罩层在window居中
                $window = $(window);
                $content.css({
                    left: ($window.width() - width) / 2,
                    top: ($window.height() - height) / 2
                });
            } else {
                //让遮罩层在$target中居中
                $content.css({
                    left: ($target.width() - width) / 2,
                    top: ($target.height() - height) / 2
                });
            }
        }

        if (!isNaN(timeout)) {
            var p = setTimeout(function () {
                $target.unmask(callback);
            }, timeout);
        }

        if (isclickclose) {
            //点击遮罩背景自动关闭
            $(".rhui-mask").click(function () {
                $(".rhui-mask").unbind();
                $target.unmask(callback);
                if (!isNaN(timeout)) {
                    clearTimeout(p);
                }
            });
        }


    },

    /**
     * 取消遮罩层
     */
    unmask: function (callback) {
        var $target;

        if (this.length === 0) {
            $target = $('body');
        } else {
            $target = this.first();
            if ($target[0] === window || $target[0] === document) {
                $target = $('body');
            }
        }

        var data = $target.data('rhui.mask');
        if (!data) {
            return;
        }

        //还原目标元素的position属性
        if (data.targetStatic) {
            $target.css('position', 'static');
        }

        data.$overlay.remove();
        data.$content.remove();

        $target.removeData('rhui.mask');

        //判断并执行回调
        if ($.isFunction(callback)) {
            callback();
        }

        $("#selector").val("");
    }
});
