package com.ashwithkumar.tvhome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** Draws the tvOS style lighter band behind the first (dock) row and adds a gap below it. */
public class DockDecoration extends RecyclerView.ItemDecoration {

    private final int dockSize;
    private final float density;
    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF outerRect = new RectF();

    /** Extra band below the tiles for their names (always_show_names), in px. */
    private final float namesRoom;
    /** Light glass like the top bar's pills (config.xml dock_glass): a white tint that fades downwards. */
    private final boolean lightGlass;
    /** Padding all round the tiles equal to the gap between them (config.xml dock_even_padding). */
    private final boolean evenPadding;
    /** Draw the band and its edge (config.xml dock_band); false leaves the tiles floating on the wallpaper. */
    private final boolean bandVisible;
    /** single_row: the tiles after the dock share its row, so they take the same offsets to stay level with it. */
    private final boolean singleRow;
    /** dock_band_all: in a single row, the band runs behind every app instead of the dock apps only. */
    private final boolean bandAll;
    /** Draw the border line around the dock band (dock_border). */
    private final boolean bandBorder;

    /**
     * @param translucent the cinematic shelf's band: the artwork and the glow show through it.
     * @param names       the dock tiles show their names, so the band reaches down over them.
     * @param opacity     the plain band's opacity in percent (dock_opacity), so a wallpaper can show through it.
     * @param lightGlass  a light frosted band like the pills, for a sky or wallpaper behind it.
     * @param evenPadding the band's padding is the gap between the tiles on all four sides (dock_even_padding).
     * @param bandVisible false skips the band and its edge, so the tiles float on the wallpaper (dock_band).
     * @param singleRow   every app sits in the dock's row (single_row); the band covers the dock apps only, unless bandAll.
     * @param bandAll     with singleRow, the band runs behind every app in the row (dock_band_all).
     * @param bandBorder  false removes the border line around the dock band.
     */
    DockDecoration(int dockSize, float density, boolean translucent, boolean names, int opacity, boolean lightGlass,
                   boolean evenPadding, boolean bandVisible, boolean singleRow, boolean bandAll, boolean bandBorder) {
        this.dockSize = dockSize;
        this.singleRow = singleRow;
        this.bandAll = singleRow && bandAll;
        this.bandBorder = bandBorder;
        this.density = density;
        this.namesRoom = names ? 22 * density : 0;
        this.lightGlass = lightGlass;
        this.evenPadding = evenPadding;
        this.bandVisible = bandVisible;
        // Translucent dock container fill (increased transparency: ~7% opacity / 93% transparency)
        band.setStyle(Paint.Style.FILL);
        band.setColor(0x12FFFFFF);
        // Container border stroke (ultra-thin subtle glass hairline)
        outerStroke.setStyle(Paint.Style.STROKE);
        outerStroke.setStrokeWidth(1.0f);
        outerStroke.setColor(0x28FFFFFF);
    }

    DockDecoration(int dockSize, float density, boolean translucent, boolean names, int opacity, boolean lightGlass,
                   boolean evenPadding, boolean bandVisible, boolean singleRow, boolean bandAll) {
        this(dockSize, density, translucent, names, opacity, lightGlass, evenPadding, bandVisible, singleRow, bandAll, false);
    }

    /** Concentric corners: the band's radius is the tiles' radius plus the band's padding around them. */
    static float radiusPx(float tileRadiusPx, float paddingPx) {
        return tileRadiusPx + paddingPx;
    }

    /**
     * The band's padding on every side for dock_even_padding: the gap between two dock tiles (a cell minus its tile, as
     * AppAdapter.tileWidthPx lays them out), capped by the room the grid leaves at its edges.
     */
    static float evenPaddingPx(int innerWidthPx, int columns, int tileWidthPx, float roomPx) {
        float gap = columns > 0 ? innerWidthPx / (float) columns - tileWidthPx : 0f;
        return Math.max(0f, Math.min(gap, roomPx));
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos >= 0 && (singleRow || pos < dockSize)) {
            outRect.top = (int) (6 * density);
            outRect.bottom = (int) (14 * density);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (!bandVisible) return;
        boolean any = false;
        int cardWidth = 0;
        int cardHeight = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos < 0 || (!bandAll && pos >= dockSize)) continue;
            RecyclerView.ViewHolder vh = parent.getChildViewHolder(child);
            View card = vh instanceof AppAdapter.Holder ? ((AppAdapter.Holder) vh).card : child.findViewById(R.id.card);
            if (card == null) continue;
            float l = child.getLeft() + card.getLeft();
            float t = child.getTop() + card.getTop() + child.getTranslationY();
            float r = l + card.getWidth();
            float b = t + card.getHeight();
            cardWidth = card.getWidth();
            cardHeight = card.getHeight();
            if (!any) {
                rect.set(l, t, r, b);
                any = true;
            } else {
                rect.union(l, t, r, b);
            }
        }
        if (!any) return;
        // Equal margins when expanded: focus scale is 1.18f.
        // Card expands horizontally by cardWidth * (1.18f - 1f) / 2f.
        // Card expands vertically by cardHeight * (1.18f - 1f) / 2f.
        // With margin = 12dp, the gap between the expanded card and the dock capsule edge
        // is exactly 12dp on all sides: top, bottom, left (for first app), right (for last app).
        float cw = cardWidth > 0 ? cardWidth : 330f;
        float ch = cardHeight > 0 ? cardHeight : 185f;
        float expX = cw * 0.18f / 2f;
        float expY = ch * 0.18f / 2f;
        float margin = 9.5f * density;
        float pad = expY + margin;
        float sidePad = expX + margin;
        float radius = 16f * density * 1.18f + margin;
        rect.inset(-sidePad, -pad);

        if (singleRow) {
            rect.left = Math.max(rect.left, density);
            rect.right = Math.min(rect.right, parent.getWidth() - density);
        }
        rect.bottom += namesRoom;

        outerRect.set(rect);
        // Align dock container margins with top icons container and popup containers (exact 56dp side margins)
        outerRect.left = 1f;
        outerRect.right = parent.getWidth() - 1f;
        float outerRadius = radius;

        // Draw the dock container (outer container only, inner container removed)
        c.drawRoundRect(outerRect, outerRadius, outerRadius, band);
        c.drawRoundRect(outerRect, outerRadius, outerRadius, outerStroke);
    }
}
