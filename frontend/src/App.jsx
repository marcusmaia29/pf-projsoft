import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const API = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');

export default function App() {
  const { isLoading, isAuthenticated, user, error: authError, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0();
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function check(path) {
    setLoading(true); setError(''); setResult(null);
    try {
      const token = await getAccessTokenSilently();
      const response = await fetch(`${API}${path}`, { headers: { Authorization: `Bearer ${token}` }, cache: 'no-store' });
      if (response.status === 403) throw new Error('403: usuário autenticado, mas sem a role ADMIN.');
      if (response.status === 401) throw new Error('401: token ausente, inválido ou expirado. Entre novamente.');
      if (!response.ok) throw new Error(`Falha ao acessar a API: HTTP ${response.status}`);
      setResult(await response.json());
    } catch (failure) { setError(failure.message || 'Falha na conexão com a API.'); }
    finally { setLoading(false); }
  }

  if (isLoading) return <main className="loading-page"><span className="spinner" /><p>Preparando a base…</p></main>;
  return <div className="app-shell"><aside className="sidebar"><a className="brand" href="/"><span className="brand-icon">p</span><span>prova<span className="brand-dot">.</span></span></a><div className="workspace-label">PROJETO DE SOFTWARE</div><a className="nav-item active" href="/">Base da PF</a><div className="sidebar-bottom"><p>Projeto separado<br /><span>pf-projsoft</span></p></div></aside><div className="main-area"><header className="topbar"><span className="breadcrumb">PF / <strong>Base de desenvolvimento</strong></span>{isAuthenticated && <button className="button secondary" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Sair</button>}</header><main className="content"><section className="page-heading"><div><div className="eyebrow">PRONTO PARA RECEBER O ENUNCIADO</div><h1>Seu ponto de partida<span className="heading-dot">.</span></h1><p>Login, API, banco e deploy preparados. Implemente aqui as regras da prova.</p></div></section>{!isAuthenticated ? <section className="evaluations-panel"><div className="empty-state"><h3>Entre para verificar a base</h3><p>A autenticação usa o Auth0. As entidades e telas específicas serão criadas após o enunciado.</p><button className="button primary" onClick={() => loginWithRedirect({ authorizationParams: { prompt: 'login' } }).catch(failure => setError(failure.message))}>Entrar com Auth0</button></div></section> : <><section className="stats"><article className="stat-card"><span>Conta conectada</span><strong style={{fontSize:17,overflowWrap:'anywhere'}}>{user?.email || user?.name}</strong><small>Identidade confirmada pelo Auth0</small></article><article className="stat-card tint"><span>Projeto</span><strong style={{fontSize:23}}>pf-projsoft</strong><small>Sem entidades do simulado</small></article><article className="stat-card"><span>Próxima etapa</span><strong style={{fontSize:23}}>Enunciado</strong><small>Implemente as regras do enunciado</small></article></section><section className="evaluations-panel"><div className="panel-heading"><div><h2>Verificar autenticação e permissões</h2><p>Os testes abaixo acessam rotas reais da API.</p></div></div><div style={{padding:26,display:'flex',gap:12,flexWrap:'wrap'}}><button className="button primary" disabled={loading} onClick={() => check('/base/status')}>Testar acesso autenticado</button><button className="button secondary" disabled={loading} onClick={() => check('/base/admin')}>Testar acesso ADMIN</button></div>{loading && <p role="status" style={{padding:'0 26px 20px'}}>Consultando a API…</p>}{result && <pre role="status" style={{padding:26,margin:0,background:'#edf2e9',whiteSpace:'pre-wrap',overflowWrap:'anywhere',fontSize:13}}>{JSON.stringify(result,null,2)}</pre>}</section></>}{(error || authError) && <div className="alert error" role="alert" style={{marginTop:20}}>{error || authError.message}</div>}<footer className="footer"><span>PF · Projeto de Software</span><span>Base limpa, pronta para adaptar.</span></footer></main></div></div>;
}
