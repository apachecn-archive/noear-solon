package org.noear.solon.i18n;

import org.noear.solon.core.Aop;
import org.noear.solon.core.handle.Context;

import java.util.*;

/**
 * 国际化工具
 *
 * @author noear
 * @since 1.5
 */
public class I18nUtils {
    private static I18nBundleFactory bundleFactory = new I18nBundleFactoryLocal();
    private static LocaleResolver localeResolver = new LocaleResolverOfHeader();

    private static final String MESSAGE_BUNDLE = "i18n.message";
    private static final Map<Locale, I18nBundle> MESSAGE_MAP = new HashMap<>();

    static {
        Aop.getAsyn(I18nBundleFactory.class, bw -> {
            bundleFactory = bw.raw();
        });

        Aop.getAsyn(LocaleResolver.class, bw -> {
            localeResolver = bw.raw();
        });
    }


    /**
     * 获取国际化包
     * */
    public static I18nBundle getBundle(String bundleName, Locale locale) {
        return bundleFactory.create(bundleName, locale);
    }

    /**
     * 获取国际化包
     * */
    public static I18nBundle getBundle(String bundleName, Context ctx) {
        Locale locale = ctx.getLocale();
        if (locale == null) {
            locale = localeResolver.getLocale(ctx);
        }

        return getBundle(bundleName, locale);
    }

    /**
     * 获取国际化消息
     * */
    public static String getMessage(Locale locale, String key) {
        return getMessage(locale, key, null);
    }

    /**
     * 获取国际化消息
     * */
    public static String getMessage(Locale locale, String key, Object[] args) {
        if(locale == null){
            locale = Locale.getDefault();
        }

        I18nBundle bundle = MESSAGE_MAP.get(locale);
        if (bundle == null) {
            bundle = getBundle(MESSAGE_BUNDLE, locale);
            MESSAGE_MAP.put(locale, bundle);
        }

        if (args == null || args.length == 0) {
            return bundle.get(key);
        } else {
            return bundle.getAndFormat(key, args);
        }
    }
}
