// Generated code from Butter Knife. Do not modify!
package com.lvshandian.asktoask.module.setting;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class AreaActivity$$ViewBinder<T extends com.lvshandian.asktoask.module.setting.AreaActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558724, "field 'tvTitlebarCentertext'");
    target.tvTitlebarCentertext = finder.castView(view, 2131558724, "field 'tvTitlebarCentertext'");
    view = finder.findRequiredView(source, 2131558725, "field 'llTitlebarZuojiantou'");
    target.llTitlebarZuojiantou = finder.castView(view, 2131558725, "field 'llTitlebarZuojiantou'");
    view = finder.findRequiredView(source, 2131558726, "field 'tvTitlebarRighttext'");
    target.tvTitlebarRighttext = finder.castView(view, 2131558726, "field 'tvTitlebarRighttext'");
    view = finder.findRequiredView(source, 2131558609, "field 'lvSelectorcityChengshiliebiao'");
    target.lvSelectorcityChengshiliebiao = finder.castView(view, 2131558609, "field 'lvSelectorcityChengshiliebiao'");
  }

  @Override public void unbind(T target) {
    target.tvTitlebarCentertext = null;
    target.llTitlebarZuojiantou = null;
    target.tvTitlebarRighttext = null;
    target.lvSelectorcityChengshiliebiao = null;
  }
}
