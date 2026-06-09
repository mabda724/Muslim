package com.Blackbox.muslim.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.Blackbox.muslim.R

class AnimationHelper(private val context: Context) {

    companion object {
        fun createConfettiEffect(view: View, colorRes: Int = R.color.gold_400) {
            val context = view.context
            val parent = view.parent as? android.view.ViewGroup ?: return

            // Create simple confetti particles
            for (i in 0 until 20) {
                val particle = View(context)
                particle.setBackgroundColor(ContextCompat.getColor(context, colorRes))
                val size = (10..20).random()
                particle.layoutParams = android.view.ViewGroup.LayoutParams(size, size)

                // Position particle at center of view
                val startX = view.x + view.width / 2
                val startY = view.y + view.height / 2
                particle.x = startX
                particle.y = startY

                parent.addView(particle)

                // Animate particle
                val endX = startX + ( -200..200 ).random().toFloat()
                val endY = startY + ( -300..-100 ).random().toFloat()
                val rotation = (0..360).random().toFloat()

                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(particle, "x", endX),
                        ObjectAnimator.ofFloat(particle, "y", endY),
                        ObjectAnimator.ofFloat(particle, "rotation", rotation),
                        ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f)
                    )
                    duration = 1000
                    interpolator = AccelerateDecelerateInterpolator()

                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            parent.removeView(particle)
                        }
                    })
                    start()
                }
            }
        }

        fun createLevelUpEffect(view: View) {
            // Scale up and down animation
            val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f)
            val scaleDown = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f)
            val scaleBackX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f)
            val scaleBackY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f)

            AnimatorSet().apply {
                playTogether(scaleUp, scaleDown)
                play(scaleBackX).after(scaleUp)
                play(scaleBackY).after(scaleDown)
                duration = 200
                interpolator = BounceInterpolator()
                start()
            }
        }

        fun createSuccessPulse(view: View) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                duration = 300
                interpolator = DecelerateInterpolator()
                start()
            }
        }

        fun createSlideInFromRight(view: View) {
            view.translationX = view.resources.displayMetrics.widthPixels.toFloat()
            view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        fun createSlideInFromLeft(view: View) {
            view.translationX = -view.resources.displayMetrics.widthPixels.toFloat()
            view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        fun createFadeInScale(view: View) {
            view.scaleX = 0f
            view.scaleY = 0f
            view.alpha = 0f
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    fun showAchievementUnlocked(title: String, description: String) {
        Toast.makeText(context, "🏆 إنجاز جديد: $title\n$description", Toast.LENGTH_LONG).show()
    }
}
