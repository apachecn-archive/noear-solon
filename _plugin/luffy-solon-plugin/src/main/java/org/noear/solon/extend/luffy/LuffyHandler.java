package org.noear.solon.extend.luffy;

import org.noear.luffy.executor.ExecutorFactory;
import org.noear.luffy.model.AFileModel;
import org.noear.luffy.utils.TextUtils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.extend.luffy.impl.JtRun;

/**
 * @author noear 2021/3/28 created
 */
public class LuffyHandler implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        String path = ctx.path();

        do_handle(path, ctx, false);
    }

    private void do_handle(String path, Context ctx, boolean debug) throws Exception {

        AFileModel file = JtRun.fileGet(path);

        //文件不存在，则404
        if (file == null || file.path == null) {
            ctx.status(404);
            return;
        }

        if (file.is_disabled && debug == false) {
            ctx.status(403);
            return;
        }

        if (file.content_type != null && file.content_type.startsWith("code/")) {
            ctx.status(403);
            return;
        }

        //如果有跳转，则跳转
        if (TextUtils.isEmpty(file.link_to) == false) {
            if (file.link_to.startsWith("@")) {
                do_handle(file.link_to.substring(1), ctx, debug);
            } else {
                ctx.redirect(file.link_to);
            }
            return;
        }

        ctx.attrSet("file", file);


        String name = path.replace("/", "__");
        ExecutorFactory.exec(name, file, ctx);
    }
}