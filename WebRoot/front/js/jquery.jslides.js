jQuery(function($){
    if ($(".slides").length > 0) {
        var defaultOpts = { interval: 5000, fadeInTime: 200, fadeOutTime: 300 };
        var _pagination = $(".pagination li");
        var _slides = $(".slides li");
        var _count = _pagination.length;
        var _current = 0;
        var _intervalID = null;
        var stop = function () { window.clearInterval(_intervalID); };
        var slide = function (opts) {
            if (opts) {
                _current = opts.current || 0;
            } else {
                _current = (_current >= (_count - 1)) ? 0 : (++_current);
            };
            _slides.filter(":visible").fadeOut(defaultOpts.fadeOutTime, function () {
                _slides.eq(_current).fadeIn(defaultOpts.fadeInTime);
                _slides.removeClass("curr").eq(_current).addClass("curr");
            });
            _pagination.removeClass("curr").eq(_current).addClass("curr");
        };
        var go = function () {
            stop();
            _intervalID = window.setInterval(function () { slide(); }, defaultOpts.interval);
        };
        var itemMouseOver = function (target, items) {
            stop();
            var i = $.inArray(target, items);
            slide({ current: i });
        };
        _pagination.hover(function () { if ($(this).attr('class') != 'curr') { itemMouseOver(this, _pagination); } else { stop(); } }, go);
        _slides.hover(stop, go);
        go();
    }
});