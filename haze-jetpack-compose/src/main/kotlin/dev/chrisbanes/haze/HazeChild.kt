// Copyright 2023, Christopher Banes and the Haze project contributors
// SPDX-License-Identifier: Apache-2.0

package dev.chrisbanes.haze

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

/**
 * Mark this composable as being a Haze child composable.
 *
 * This will update the given [HazeState] whenever the layout is placed, enabling any layouts using
 * [Modifier.haze] to blur any content behind the host composable.
 */
fun Modifier.hazeChild(
  key: Any,
  state: HazeState,
  shape: Shape = RectangleShape,
): Modifier = this then HazeChildNodeElement(key, state, shape)

private data class HazeChildNodeElement(
  val key: Any,
  val state: HazeState,
  val shape: Shape,
) : ModifierNodeElement<HazeChildNode>() {
  override fun create(): HazeChildNode = HazeChildNode(key, state, shape)

  override fun update(node: HazeChildNode) {
    node.key = key
    node.state = state
    node.shape = shape
    node.onUpdate()
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "HazeChild"
    properties["key"] = key
    properties["shape"] = shape
  }
}

private data class HazeChildNode(
  var key: Any,
  var state: HazeState,
  var shape: Shape,
) : Modifier.Node(), LayoutAwareModifierNode {
  override fun onPlaced(coordinates: LayoutCoordinates) {
    // After we've been placed, update the state with our new bounds (in root coordinates)
    state.updateAreaPosition(key, coordinates.positionInRoot())
  }

  override fun onAttach() {
    state.updateAreaShape(key, shape)
  }

  fun onUpdate() {
    state.updateAreaShape(key, shape)
  }

  override fun onRemeasured(size: IntSize) {
    // After we've been remeasured, update the state with our new size
    state.updateAreaSize(key, size.toSize())
  }

  override fun onReset() {
    state.clearArea(key)
  }

  override fun onDetach() {
    state.clearArea(key)
  }
}