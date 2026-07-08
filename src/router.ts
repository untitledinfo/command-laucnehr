import { createRouter, createWebHashHistory } from 'vue-router';
import Home from './views/Home.vue';
import Mods from './views/Mods.vue';
import Console from './views/Console.vue';
import Settings from './views/Settings.vue';

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/mods', name: 'mods', component: Mods },
    { path: '/console', name: 'console', component: Console },
    { path: '/settings', name: 'settings', component: Settings }
  ]
});
