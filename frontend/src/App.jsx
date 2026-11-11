import React, { useCallback, useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const API = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');
const ROLES_CLAIM = 'https://social-insper.com/roles';
const PRIORIDADES = ['Alta', 'Média', 'Baixa'];
const FORM_VAZIO = { titulo: '', descricao: '', prioridade: 'Média' };

function rolesDoToken(token) {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const roles = JSON.parse(atob(payload))[ROLES_CLAIM];
    return Array.isArray(roles) ? roles : [];
  } catch {
    return [];
  }
}

async function mensagemDeErro(response) {
  if (response.status === 401) return 'Sessão inválida ou expirada. Entre novamente.';
  if (response.status === 403) return 'Você não tem permissão para realizar esta ação.';
  try {
    const body = await response.json();
    if (body?.erro) return body.erro;
  } catch {
    // resposta sem corpo JSON
  }
  return `Falha ao acessar a API (HTTP ${response.status}).`;
}

export default function App() {
  const { isLoading, isAuthenticated, user, error: authError, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0();
  const [artigos, setArtigos] = useState([]);
  const [admin, setAdmin] = useState(false);
  const [form, setForm] = useState(FORM_VAZIO);
  const [carregando, setCarregando] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [removendo, setRemovendo] = useState(null);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');

  const chamar = useCallback(async (path, options = {}) => {
    const token = await getAccessTokenSilently();
    setAdmin(rolesDoToken(token).includes('ADMIN'));
    const response = await fetch(`${API}${path}`, {
      ...options,
      cache: 'no-store',
      headers: {
        Authorization: `Bearer ${token}`,
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      },
    });
    if (!response.ok) throw new Error(await mensagemDeErro(response));
    return response.status === 204 ? null : response.json();
  }, [getAccessTokenSilently]);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro('');
    try {
      setArtigos(await chamar('/artigos'));
    } catch (falha) {
      setErro(falha.message || 'Falha na conexão com a API.');
    } finally {
      setCarregando(false);
    }
  }, [chamar]);

  useEffect(() => {
    if (isAuthenticated) carregar();
  }, [isAuthenticated, carregar]);

  async function criar(event) {
    event.preventDefault();
    setSalvando(true);
    setErro('');
    setSucesso('');
    try {
      const criado = await chamar('/artigos', { method: 'POST', body: JSON.stringify(form) });
      setArtigos((atuais) => [...atuais, criado]);
      setForm(FORM_VAZIO);
      setSucesso('Artigo criado.');
    } catch (falha) {
      setErro(falha.message || 'Falha na conexão com a API.');
    } finally {
      setSalvando(false);
    }
  }

  async function deletar(artigo) {
    setRemovendo(artigo.id);
    setErro('');
    setSucesso('');
    try {
      await chamar(`/artigos/${artigo.id}`, { method: 'DELETE' });
      setArtigos((atuais) => atuais.filter((item) => item.id !== artigo.id));
      setSucesso('Artigo deletado.');
    } catch (falha) {
      setErro(falha.message || 'Falha na conexão com a API.');
    } finally {
      setRemovendo(null);
    }
  }

  function alterar(campo) {
    return (event) => setForm((atual) => ({ ...atual, [campo]: event.target.value }));
  }

  if (isLoading) {
    return <main className="loading-page"><span className="spinner" /><p>Carregando…</p></main>;
  }

  const mensagemErro = erro || authError?.message;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="/">
          <span className="brand-icon">b</span>
          <span>base<span className="brand-dot">.</span></span>
        </a>
        <div className="workspace-label">BASE DE CONHECIMENTOS</div>
        <a className="nav-item active" href="/">Artigos</a>
        {isAuthenticated && (
          <div className="sidebar-bottom">
            <p>{admin ? 'Administrador' : 'Usuário'}<br /><span>{user?.email || user?.name}</span></p>
          </div>
        )}
      </aside>

      <div className="main-area">
        <header className="topbar">
          <span className="breadcrumb">Base de conhecimentos / <strong>Artigos</strong></span>
          {isAuthenticated && (
            <button className="button secondary" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
              Sair
            </button>
          )}
        </header>

        <main className="content">
          <section className="page-heading">
            <div>
              <div className="eyebrow">BASE DE CONHECIMENTOS</div>
              <h1>Artigos<span className="heading-dot">.</span></h1>
              <p>Consulte os artigos da empresa. Administradores também criam e deletam artigos.</p>
            </div>
          </section>

          {mensagemErro && <div className="alert error" role="alert">{mensagemErro}</div>}
          {sucesso && <div className="alert success" role="status">{sucesso}</div>}

          {!isAuthenticated ? (
            <section className="evaluations-panel">
              <div className="empty-state">
                <h3>Entre para acessar os artigos</h3>
                <p>O acesso à base de conhecimentos exige autenticação.</p>
                <button
                  className="button primary"
                  onClick={() => loginWithRedirect({ authorizationParams: { prompt: 'login' } }).catch((falha) => setErro(falha.message))}
                >
                  Entrar com Auth0
                </button>
              </div>
            </section>
          ) : (
            <>
              {admin && (
                <section className="evaluations-panel article-form-panel">
                  <div className="panel-heading">
                    <div>
                      <h2>Novo artigo</h2>
                      <p>O e-mail do criador é registrado a partir da sua conta.</p>
                    </div>
                  </div>
                  <form className="article-form" onSubmit={criar}>
                    <label>
                      <span>Título</span>
                      <input value={form.titulo} onChange={alterar('titulo')} maxLength={200} required />
                    </label>
                    <label>
                      <span>Prioridade</span>
                      <select value={form.prioridade} onChange={alterar('prioridade')} required>
                        {PRIORIDADES.map((prioridade) => <option key={prioridade}>{prioridade}</option>)}
                      </select>
                    </label>
                    <label className="wide">
                      <span>Descrição</span>
                      <textarea value={form.descricao} onChange={alterar('descricao')} maxLength={4000} required />
                    </label>
                    <div className="wide article-form-actions">
                      <button className="button primary" type="submit" disabled={salvando}>
                        {salvando ? 'Criando…' : 'Criar artigo'}
                      </button>
                    </div>
                  </form>
                </section>
              )}

              <section className="evaluations-panel">
                <div className="panel-heading">
                  <div>
                    <h2>Artigos <span className="count-badge">{artigos.length}</span></h2>
                    <p>{admin ? 'Você pode criar e deletar artigos.' : 'Seu perfil permite apenas listar os artigos.'}</p>
                  </div>
                  <button className="button secondary" onClick={carregar} disabled={carregando}>
                    {carregando ? 'Atualizando…' : 'Atualizar'}
                  </button>
                </div>

                {artigos.length === 0 ? (
                  <div className="empty-state">
                    <h3>{carregando ? 'Carregando artigos…' : 'Nenhum artigo cadastrado'}</h3>
                    {!carregando && <p>Os artigos criados aparecem aqui.</p>}
                  </div>
                ) : (
                  <div className="evaluation-list">
                    {artigos.map((artigo) => (
                      <article className="evaluation" key={artigo.id}>
                        <div className="evaluation-person">
                          <div>
                            <div className="field-label">ARTIGO #{artigo.id}</div>
                            <h3>{artigo.titulo}</h3>
                          </div>
                          <div className="grade article-priority"><span>Prioridade</span><strong>{artigo.prioridade}</strong></div>
                        </div>
                        <p className="comment">{artigo.descricao}</p>
                        <div className="evaluation-footer">
                          <span>Criado por <strong>{artigo.emailCriador}</strong></span>
                          {admin && (
                            <button className="button secondary danger" onClick={() => deletar(artigo)} disabled={removendo === artigo.id}>
                              {removendo === artigo.id ? 'Deletando…' : 'Deletar'}
                            </button>
                          )}
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </>
          )}

          <footer className="footer">
            <span>Base de conhecimentos</span>
            <span>Projeto de Software</span>
          </footer>
        </main>
      </div>
    </div>
  );
}
