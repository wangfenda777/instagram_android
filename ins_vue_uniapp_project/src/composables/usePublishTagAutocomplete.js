import { computed, ref } from 'vue'
import { searchTag } from '@/api/search.js'

const SEARCH_DEBOUNCE_DELAY = 250
const DEFAULT_TEXTAREA_HEIGHT = 200

const getList = (payload) => {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.list)) return payload.list
  return []
}

const normalizeTagName = (value = '') => value.replace(/^#+/, '')

const normalizeTag = (item = {}) => ({
  id: item.tagId || item.id || item.tagName || item.name || '',
  name: normalizeTagName(item.tagName || item.name || ''),
  postCount: Number(item.postCount || item.count || item.noteCount || 0)
})

const parseTagSegments = (text = '') => {
  const source = text || ''
  const regex = /#[^\s#]+/g
  const segments = []
  let lastIndex = 0
  let match = regex.exec(source)

  while (match) {
    const start = match.index
    const end = start + match[0].length

    if (start > lastIndex) {
      segments.push({
        type: 'text',
        text: source.slice(lastIndex, start)
      })
    }

    segments.push({
      type: 'tag',
      text: match[0]
    })

    lastIndex = end
    match = regex.exec(source)
  }

  if (lastIndex < source.length) {
    segments.push({
      type: 'text',
      text: source.slice(lastIndex)
    })
  }

  if (!segments.length) {
    segments.push({
      type: 'text',
      text: source
    })
  }

  return segments
}

const findActiveTagToken = (text = '', cursor = 0) => {
  const safeCursor = Math.max(0, Math.min(cursor, text.length))
  const beforeCursor = text.slice(0, safeCursor)
  const hashIndex = beforeCursor.lastIndexOf('#')

  if (hashIndex === -1) return null

  const prefix = beforeCursor.slice(hashIndex, safeCursor)
  if (/\s/.test(prefix)) return null

  const previousChar = hashIndex > 0 ? text[hashIndex - 1] : ''
  if (previousChar && !/\s/.test(previousChar)) return null

  let tokenEnd = safeCursor
  while (tokenEnd < text.length && !/\s/.test(text[tokenEnd])) {
    tokenEnd += 1
  }

  const token = text.slice(hashIndex, tokenEnd)
  if (!token.startsWith('#')) return null
  if (/#/.test(token.slice(1))) return null

  return {
    start: hashIndex,
    end: tokenEnd,
    token,
    keyword: token.slice(1)
  }
}

export function usePublishTagAutocomplete(content) {
  const tagSuggestions = ref([])
  const isSearchingTags = ref(false)
  const tagSearchError = ref('')
  const activeToken = ref(null)
  const caretPosition = ref(0)
  const textareaHeight = ref(DEFAULT_TEXTAREA_HEIGHT)

  let searchTimer = null
  let activeRequestId = 0

  const formattedSegments = computed(() => parseTagSegments(content.value))
  const shouldShowSuggestions = computed(() => {
    return Boolean(activeToken.value) && (tagSuggestions.value.length > 0 || isSearchingTags.value || tagSearchError.value)
  })

  const clearSuggestions = () => {
    tagSuggestions.value = []
    tagSearchError.value = ''
    isSearchingTags.value = false
  }

  const updateTextareaHeight = (value = '') => {
    const lines = Math.max(1, value.split('\n').length)
    textareaHeight.value = Math.max(DEFAULT_TEXTAREA_HEIGHT, lines * 42 + 60)
  }

  const searchTags = async (keyword) => {
    const requestId = ++activeRequestId
    isSearchingTags.value = true
    tagSearchError.value = ''

    try {
      const payload = await searchTag(keyword)
      if (requestId !== activeRequestId) return
      tagSuggestions.value = getList(payload).map(normalizeTag).filter(item => item.name)
    } catch (error) {
      if (requestId !== activeRequestId) return
      tagSuggestions.value = []
      tagSearchError.value = '标签搜索失败'
    } finally {
      if (requestId === activeRequestId) {
        isSearchingTags.value = false
      }
    }
  }

  const scheduleTagSearch = (tokenInfo) => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }

    if (!tokenInfo) {
      activeRequestId += 1
      clearSuggestions()
      return
    }

    activeToken.value = tokenInfo

    if (!tokenInfo.keyword.trim()) {
      activeRequestId += 1
      clearSuggestions()
      return
    }

    searchTimer = setTimeout(() => {
      searchTags(tokenInfo.keyword.trim())
    }, SEARCH_DEBOUNCE_DELAY)
  }

  const handleContentInput = (event) => {
    const value = event?.detail?.value ?? ''
    const cursor = typeof event?.detail?.cursor === 'number' ? event.detail.cursor : value.length
    content.value = value
    caretPosition.value = cursor
    updateTextareaHeight(value)

    const tokenInfo = findActiveTagToken(value, cursor)
    activeToken.value = tokenInfo
    scheduleTagSearch(tokenInfo)
  }

  const applyTagSuggestion = (tag) => {
    if (!activeToken.value || activeToken.value.start < 0) return

    const selectedTag = `#${tag.name}`
    const nextContent = `${content.value.slice(0, activeToken.value.start)}${selectedTag} ${content.value.slice(activeToken.value.end)}`

    content.value = nextContent
    caretPosition.value = activeToken.value.start + selectedTag.length + 1
    activeToken.value = null
    activeRequestId += 1
    clearSuggestions()
    updateTextareaHeight(nextContent)
  }

  const hideSuggestions = () => {
    activeToken.value = null
    activeRequestId += 1
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    clearSuggestions()
  }

  updateTextareaHeight(content.value)

  return {
    textareaHeight,
    caretPosition,
    formattedSegments,
    tagSuggestions,
    isSearchingTags,
    tagSearchError,
    shouldShowSuggestions,
    handleContentInput,
    applyTagSuggestion,
    hideSuggestions
  }
}
