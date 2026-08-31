package org.levimc.launcher.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.levimc.launcher.R;
import org.levimc.launcher.core.mods.inbuilt.UnifiedMod;
import org.levimc.launcher.core.mods.inbuilt.overlay.InbuiltOverlayManager;
import org.levimc.launcher.core.mods.inbuilt.overlay.ModConfigView;
import org.levimc.launcher.util.PersonalizationManager;

import java.util.ArrayList;
import java.util.List;

public class UnifiedModAdapter extends RecyclerView.Adapter<UnifiedModAdapter.ViewHolder> {

    private final Activity activity;
    private List<UnifiedMod> mods = new ArrayList<>();
    private OnModToggleListener listener;

    public interface OnModToggleListener {
        void onModToggled(UnifiedMod mod, boolean enabled);
    }

    public UnifiedModAdapter(Activity activity, List<UnifiedMod> mods) {
        this.activity = activity;
        this.mods = mods != null ? mods : new ArrayList<>();
    }

    public void setOnModToggleListener(OnModToggleListener listener) {
        this.listener = listener;
    }

    public void updateMods(List<UnifiedMod> list) {
        this.mods = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnifiedMod mod = mods.get(position);
        Context context = holder.itemView.getContext();

        if (holder.dragHandle != null) {
            holder.dragHandle.setVisibility(View.GONE);
        }
        if (holder.orderText != null) {
            holder.orderText.setVisibility(View.GONE);
        }

        holder.name.setText(mod.getName());
        holder.authorText.setText(mod.getGroupName());
        holder.versionText.setText("Built-in");

        if (holder.configBadge != null) {
            if (mod.hasConfig()) {
                holder.configBadge.setVisibility(View.VISIBLE);
                holder.configBadge.setOnClickListener(v -> showConfigDialog(mod));
            } else {
                holder.configBadge.setVisibility(View.GONE);
            }
        }

        holder.switchBtn.setOnCheckedChangeListener(null);
        holder.switchBtn.setChecked(mod.isEnabled());

        holder.switchBtn.setOnCheckedChangeListener((btn, isChecked) -> {
            InbuiltOverlayManager overlayManager = InbuiltOverlayManager.getInstance();
            if (overlayManager != null) {
                overlayManager.handleModToggle(mod.getId(), isChecked);
            }
            if (listener != null) {
                listener.onModToggled(mod, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (mod.hasConfig()) {
                showConfigDialog(mod);
            }
        });

        PersonalizationManager pm = new PersonalizationManager(context);
        pm.applyGlassToView(holder.itemView);
        pm.applyAccentToView(holder.itemView, context);
    }

    private void showConfigDialog(UnifiedMod mod) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_inbuilt_mod_config, null);
        TextView titleView = dialogView.findViewById(R.id.config_title);
        LinearLayout container = dialogView.findViewById(R.id.config_items_container);

        if (titleView != null) {
            titleView.setText(mod.getName());
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setPositiveButton(R.string.close, (d, w) -> d.dismiss())
                .create();

        if (container != null) {
            ModConfigView.render(activity, container, mod, () -> {
                InbuiltOverlayManager overlayManager = InbuiltOverlayManager.getInstance();
                if (overlayManager != null) {
                    overlayManager.applyConfigurationChanges(mod.getId());
                }
            });
        }

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return mods != null ? mods.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView orderText;
        TextView configBadge;
        TextView authorText;
        TextView versionText;
        Switch switchBtn;
        View dragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mod_name);
            orderText = itemView.findViewById(R.id.mod_order);
            configBadge = itemView.findViewById(R.id.mod_config_badge);
            authorText = itemView.findViewById(R.id.mod_author);
            versionText = itemView.findViewById(R.id.mod_version);
            switchBtn = itemView.findViewById(R.id.mod_switch);
            dragHandle = itemView.findViewById(R.id.drag_handle);
        }
    }
}
