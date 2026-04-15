const DEFAULT_FEEDBACK_DURATION = 260

export function useActionFeedback(defaultDuration = DEFAULT_FEEDBACK_DURATION) {
  const triggerFeedback = (target, key, duration = defaultDuration) => {
    if (!target || !key) return

    target[key] = false
    setTimeout(() => {
      target[key] = true
      setTimeout(() => {
        target[key] = false
      }, duration)
    }, 0)
  }

  return {
    triggerFeedback,
    defaultFeedbackDuration: defaultDuration
  }
}
