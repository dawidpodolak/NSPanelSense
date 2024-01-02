@file:Suppress("MatchingDeclarationName")

package com.panelsense.app.ui.main.panel.item

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.panelsense.app.ui.main.EntityInteractor
import com.panelsense.app.ui.main.panel.ButtonShape
import com.panelsense.app.ui.main.panel.PanelSizeHelper.PanelItemOrientation.HORIZONTAL
import com.panelsense.app.ui.main.panel.PanelSizeHelper.PanelItemOrientation.VERTICAL
import com.panelsense.app.ui.main.panel.StateLaunchEffect
import com.panelsense.app.ui.main.panel.getDrawable
import com.panelsense.app.ui.main.panel.getPanelSizeHelper
import com.panelsense.app.ui.main.panel.item.PanelItemLayoutRequest.Companion.applySizeIfFlex
import com.panelsense.app.ui.main.panel.item.PanelItemLayoutRequest.Flex
import com.panelsense.app.ui.theme.FontStyleH3_SemiBold
import com.panelsense.app.ui.theme.FontStyleH4
import com.panelsense.app.ui.theme.PanelItemBackgroundColor
import com.panelsense.app.ui.theme.PanelItemTitleColor
import com.panelsense.domain.model.PanelItem
import com.panelsense.domain.model.entity.state.SensorEntityState

data class SensorItemState(
    val icon: Drawable? = null,
    val title: String = "",
    val entityState: SensorEntityState? = null
)

@Composable
fun SensorItemView(
    modifier: Modifier,
    panelItem: PanelItem,
    entityInteractor: EntityInteractor,
    initState: SensorItemState = SensorItemState(),
    layoutRequest: PanelItemLayoutRequest
) {
    var state by remember { mutableStateOf(initState) }
    val panelSizeHelper = getPanelSizeHelper()

    StateLaunchEffect<SensorEntityState, SensorItemState>(
        panelItem = panelItem,
        entityInteractor = entityInteractor,
        mapper = ::entityToItemState
    ) {
        state = it
    }

    Box(
        modifier = modifier
            .applySizeIfFlex(layoutRequest)
            .background(
                color = PanelItemBackgroundColor,
                shape = ButtonShape
            )
            .onGloballyPositioned(panelSizeHelper::onGlobalLayout)
    ) {
        when {
            panelSizeHelper.orientation == HORIZONTAL || layoutRequest is Flex -> HorizontalSensorItemView(
                modifier = Modifier.fillMaxSize(),
                initState = state
            )

            panelSizeHelper.orientation == VERTICAL -> VerticalSensorItemView(
                modifier = Modifier.fillMaxSize(),
                initState = state
            )
        }
    }
}

@Composable
fun HorizontalSensorItemView(
    modifier: Modifier,
    initState: SensorItemState
) {
    ConstraintLayout(modifier = modifier) {
        val (title, icon, valueText) = createRefs()
        Image(
            modifier = Modifier.constrainAs(icon) {
                end.linkTo(valueText.start, margin = 2.dp)
                top.linkTo(valueText.top)
                bottom.linkTo(valueText.bottom)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
            },
            painter = rememberDrawablePainter(drawable = initState.icon),
            contentDescription = initState.title
        )
        val entityValue = initState.entityState?.run { state + unitOfMeasurement }
        if (entityValue != null) {
            Text(
                modifier = Modifier.constrainAs(valueText) {
                    end.linkTo(parent.end, margin = 30.dp)
                    top.linkTo(parent.top, margin = 15.dp)
                    bottom.linkTo(parent.bottom, margin = 15.dp)
                },
                text = entityValue ?: "",
                color = PanelItemTitleColor,
                style = FontStyleH3_SemiBold,
            )
        }

        Text(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(parent.start, margin = 30.dp)
                top.linkTo(parent.top, margin = 15.dp)
                bottom.linkTo(parent.bottom, margin = 15.dp)
                end.linkTo(valueText.start, margin = 30.dp)
                width = Dimension.fillToConstraints
            },
            text = initState.title,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = PanelItemTitleColor,
            style = FontStyleH3_SemiBold
        )
    }
}

@Composable
fun VerticalSensorItemView(
    modifier: Modifier,
    initState: SensorItemState
) {
    ConstraintLayout(modifier = modifier) {
        val (title, icon, valueText) = createRefs()
        Image(
            modifier = Modifier.constrainAs(icon) {
                end.linkTo(parent.end)
                linkTo(parent.top, valueText.top, bottomMargin = 2.dp, bias = 1f)
                start.linkTo(parent.start)
            },
            painter = rememberDrawablePainter(drawable = initState.icon),
            contentDescription = initState.title
        )
        val entityValue = initState.entityState?.run { state + unitOfMeasurement }
        if (entityValue != null) {
            Text(
                modifier = Modifier.constrainAs(valueText) {
                    end.linkTo(parent.end, margin = 30.dp)
                    start.linkTo(parent.start, margin = 30.dp)
                    top.linkTo(parent.top, margin = 15.dp)
                    bottom.linkTo(parent.bottom, margin = 15.dp)
                },
                text = entityValue ?: "",
                color = PanelItemTitleColor,
                style = FontStyleH3_SemiBold,
            )
        }

        Text(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(valueText.start)
                top.linkTo(valueText.bottom, margin = 5.dp)
                bottom.linkTo(parent.bottom, margin = 5.dp)
                end.linkTo(valueText.end)
            },
            text = initState.title,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = PanelItemTitleColor,
            overflow = TextOverflow.Ellipsis,
            style = FontStyleH4
        )
    }
}

private suspend fun entityToItemState(
    entityState: SensorEntityState,
    panelItem: PanelItem,
    entityInteractor: EntityInteractor
): SensorItemState = SensorItemState(
    icon = entityInteractor.getDrawable(
        mdiName = panelItem.icon ?: entityState.icon ?: entityState.mdiIcon,
        enabledColor = PanelItemTitleColor,
        enable = true,
    ),
    title = panelItem.title ?: entityState.friendlyName ?: "test",
    entityState = entityState
)
