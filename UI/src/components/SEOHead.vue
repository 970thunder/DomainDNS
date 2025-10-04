<template>
    <!-- SEO组件，用于动态设置页面SEO信息 -->
</template>

<script>
import { setPageSEO, getPageSEO } from '@/config/seo.js'

export default {
    name: 'SEOHead',
    props: {
        pageName: {
            type: String,
            required: true
        },
        customTitle: {
            type: String,
            default: ''
        },
        customDescription: {
            type: String,
            default: ''
        },
        customKeywords: {
            type: Array,
            default: () => []
        }
    },
    mounted() {
        this.updateSEO()
    },
    watch: {
        pageName() {
            this.updateSEO()
        },
        customTitle() {
            this.updateSEO()
        },
        customDescription() {
            this.updateSEO()
        },
        customKeywords() {
            this.updateSEO()
        }
    },
    methods: {
        updateSEO() {
            const pageSEO = getPageSEO(this.pageName)

            const seoOptions = {
                title: this.customTitle || pageSEO.title,
                description: this.customDescription || pageSEO.description,
                keywords: this.customKeywords.length > 0 ? this.customKeywords : pageSEO.keywords,
                url: window.location.href
            }

            setPageSEO(seoOptions)
        }
    }
}
</script>
