module.exports = {
    theme: {
        extend: {
            animation: {
                'clouds': 'moveClouds 60s linear infinite',
                'clouds-slow': 'moveClouds 120s linear infinite'
            },
            keyframes: {
                moveClouds: {
                    '0%': { backgroundPosition: '0 0' },
                    '100%': { backgroundPosition: '-2000px 0' }
                }
            }
        }
    }
}
