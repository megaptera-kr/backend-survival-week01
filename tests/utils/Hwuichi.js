/* eslint-disable no-underscore-dangle */

import fetch from 'node-fetch';

import { parseResponse } from './Parser';

class Hwuichi {
  options;

  constructor() {
    this.options = {
      hostname: 'localhost',
      port: 8080,
      headers: {
        'Content-Type': 'application/json',
        Connection: 'close',
      },
      insecureHTTPParser: true,
    };
  }

  _createOptions(options) {
    return {
      ...this.options,
      ...options,
      headers: {
        ...this.options.headers,
        ...options.headers,
      },
    };
  }

  async _fetch(path, options) {
    const customOptions = this._createOptions({ ...options, path });

    const { hostname, port } = customOptions;
    const url = `http://${hostname}:${port}${path}`;

    const response = await fetch(url, customOptions);

    return response;
  }

  async get(path, options) {
    const response = await this._fetch(path, {
      ...options,
      method: 'get',
    });

    return parseResponse(response);
  }

  async post(path, payload, options) {
    const response = await this._fetch(path, {
      ...options,
      method: 'post',
      body: JSON.stringify(payload),
    });

    return parseResponse(response);
  }

  async patch(path, payload, options) {
    const response = await this._fetch(path, {
      ...options,
      method: 'patch',
      body: JSON.stringify(payload),
    });

    return parseResponse(response);
  }

  async delete(path, payload, options) {
    const response = await this._fetch(path, {
      ...options,
      method: 'delete',
    });

    return parseResponse(response);
  }
}

export default new Hwuichi();
