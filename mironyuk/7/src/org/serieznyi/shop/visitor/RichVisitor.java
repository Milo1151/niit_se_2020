package org.serieznyi.shop.visitor;

import org.serieznyi.shop.item.ShopItem;

import java.util.Collection;
import java.util.Iterator;

import static java.lang.String.format;

final public class RichVisitor implements ShopVisitor {
    @Override
    public void visitShop(Collection<? extends ShopItem> shop) {
        if (shop.size() == 0) {
            say("Похоже в магазине нет товаров");

            return;
        }

        int i = 1;
        Iterator<? extends ShopItem> iterator = shop.iterator();
        while (iterator.hasNext()) {
            ShopItem item = iterator.next();
            if (i % 2 == 0) {
                iterator.remove();
                say(format("%s куплен по %s", item.getName(), item.getPrice()));
            }

            i++;
        }
    }
}
