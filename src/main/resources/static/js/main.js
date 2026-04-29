document.addEventListener('DOMContentLoaded', function () {
    const likeBtn = document.getElementById('likeBtn');
    if (likeBtn) {
        likeBtn.addEventListener('click', function () {
            const postId = this.getAttribute('data-post-id');
            const csrfToken = document.cookie.split(';')
                .find(c => c.trim().startsWith('XSRF-TOKEN='));

            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
            const headers = { 'Content-Type': 'application/json' };
            if (csrfMeta && csrfHeaderMeta) {
                headers[csrfHeaderMeta.content] = csrfMeta.content;
            }

            fetch(`/post/${postId}/like`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({})
            })
            .then(response => {
                if (response.status === 403) {
                    window.location.href = '/member/login';
                    return;
                }
                return response.json();
            })
            .then(data => {
                if (!data) return;
                document.getElementById('likeCount').textContent = data.likeCount;
                if (data.liked) {
                    likeBtn.classList.remove('btn-outline-danger');
                    likeBtn.classList.add('btn-danger');
                } else {
                    likeBtn.classList.remove('btn-danger');
                    likeBtn.classList.add('btn-outline-danger');
                }
            })
            .catch(err => console.error('좋아요 처리 중 오류:', err));
        });
    }
});
